from datetime import datetime, timedelta
import os
import scrapy


class BDS123Spider(scrapy.Spider):
    name = "bds123Spider"
    allowed_domains = ['bds123.vn']
    start_urls = ['https://bds123.vn/nha-dat-ban-ha-noi.html']

    custom_settings = {
        'FEEDS': {
            f'{os.getcwd()}/result/{name}.json': {'format': 'json', 'overwrite': True}
        }
    }

    def __init__(self, pass_date_str='', *args, **kwargs):
        super(BDS123Spider, self).__init__(*args, **kwargs)
        try:
            self.pass_date = datetime.strptime(pass_date_str, "%H:%M %d/%m/%Y")
        except ValueError:
            self.pass_date = None
        self.stop_extraction = False

    def parse(self, response):
        now_date = datetime.now()
        listbds = response.css('ul.post-listing li.item')

        for bds in listbds:
            url_value ="https://bds123.vn" + bds.css('li.item a::attr(href)').get()
            title_value = bds.css('li.item a::attr(title)').get()
            detail_value = bds.css('aside.post-aside p.summary::text').get()
            price_value = bds.css('div.clearfix span.price::text').get() + " " + response.css('span.price i::text').get()
            square_value = bds.css('div.clearfix div.info-features span.feature-item::text').get()
            date = bds.css('span.time::attr(title)').get()

            if "giờ" in date:
                hour_difference = int(date.split(" ")[0])
                difference = timedelta(hours=hour_difference)
                date_posting = now_date - difference
                print("hours")
            elif "ngày" in date:
                day_difference = int(date.split(" ")[0])
                difference = timedelta(days=day_difference)
                date_posting = now_date - difference
                print("days")
            elif "phút" in date:
                second_difference = int(date.split(" ")[0])
                difference = timedelta(seconds=second_difference)
                date_posting = now_date - difference
                print("phút")
            else:
                date_posting = datetime.strptime(date, "%H:%M %d/%m/%Y")
                print("other")
            if self.pass_date is None or date_posting > self.pass_date:
                print(True)
                yield {
                    'url': url_value,
                    'title': title_value,
                    'detail': detail_value,
                    'price': price_value,
                    'square': square_value,
                    'date': date_posting
                }


        if not self.stop_extraction:
            next_page = response.css('ul.pagination li.page-item a::attr(href)').getall()[
                len(response.css('ul.pagination li.page-item a::attr(href)').getall()) - 1]
            if next_page is not None:
                yield response.follow(next_page, callback=self.parse)
