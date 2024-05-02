from datetime import datetime, timedelta
import os
import scrapy


class HomedySpider(scrapy.Spider):
    name = "homdySpider"
    allowed_domains = ['homedy.com']
    start_urls = ['https://homedy.com/ban-nha-dat-ha-noi']

    custom_settings = {
        'FEEDS': {
            f'{os.getcwd()}/result/{name}.json': {'format': 'json', 'overwrite': True}
        }
    }

    def __init__(self, pass_date_str='', *args, **kwargs):
        super(HomedySpider, self).__init__(*args, **kwargs)
        try:
            self.pass_date = datetime.strptime(pass_date_str, "%Y-%m-%d")
        except ValueError:
            self.pass_date = None
        self.stop_extraction = False

    def parse(self, response):
        now_date = datetime.now()
        listbds = response.css('div.tab-content div.product-item div.product-item-top')
        for bds in listbds:
            url_value ="https://homedy.com{}".format(bds.css('div.product-item-top a.title::attr(href)').get())
            title_value = bds.css('div.product-item-top a.title::text').get()
            detail_value = bds.css('div.product-item-top div.description::text').get()
            price_value = bds.css('ul.product-unit li span.price::text').get()
            square_value = bds.css('ul.product-unit li.acr span.acreage::text').get()
            date = bds.css('div.box-price-agency div.time::text').get().replace("\n","")
            if "giờ" in date:
                hour_difference = int(date.split(" ")[0])
                difference = timedelta(hours=hour_difference)
                date_posting = now_date - difference
                print(True)
            elif "Hôm qua" in date:
                date_posting = now_date - timedelta(days=1)
                print("Hôm qua")
            elif "ngày" in date:
                day_difference = int(date.split(" ")[0])
                difference = timedelta(days=day_difference)
                date_posting = now_date - difference
                print("Ngày")
            elif "tuần" in date:
                week_difference = int(date.split(" ")[0])
                difference = timedelta(weeks=week_difference)
                date_posting = now_date - difference
                print("Tuần")
            elif "Hôm nay" in date:
                date_posting = now_date
            elif "phút" in date:
                second_difference = int(date.split(" ")[0])
                difference = timedelta(seconds=second_difference)
                date_posting = now_date - difference
            else:
                continue

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
            else:
                print(False)
                self.stop_extraction = True
                break
        if not self.stop_extraction:
            next_page = "https://homedy.com" + response.css('div.page-nav ul li a[rel="next"]::attr(href)').get()
            if next_page is not None:
                yield response.follow(next_page, callback=self.parse)
