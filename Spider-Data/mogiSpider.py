from datetime import datetime, timedelta
import os
import scrapy


class MogiSpider(scrapy.Spider):
    name = "mogiSpider"
    allowed_domains = ['mogi.vn']
    start_urls = ['https://mogi.vn/ha-noi/mua-nha-dat']

    custom_settings = {
        'FEEDS': {
            f'{os.getcwd()}/result/{name}.json': {'format': 'json', 'overwrite': True}
        }
    }

    def __init__(self, pass_date_str='', *args, **kwargs):
        super(MogiSpider, self).__init__(*args, **kwargs)
        try:
            self.pass_date = datetime.strptime(pass_date_str, "%d/%m/%Y")
        except ValueError:
            self.pass_date = None
        self.stop_extraction = False

    def parse(self, response):
        listbds = response.css('ul.props li div.prop-info')
        for bds in listbds:
            url_value = bds.css('div.prop-info a.link-overlay::attr(href)').get()
            yield response.follow(url_value, callback=self.parse_bds_response)
        if not self.stop_extraction:
            next_page = response.css('ul.pagination li a::attr(href)').getall()[
                len(response.css('ul.pagination li a::attr(href)').getall()) - 1]
            if next_page is not None:
                yield response.follow(next_page, callback=self.parse)

    def parse_bds_response(self, response):
        print(response)
        now_date = datetime.now().date()
        url_value = ''.join(map(str, response.url))
        title_value = response.css('div.title h1::text').get()
        detail_value = ''.join(map(str, response.css('div.info-content-body::text').getall()))
        price_value = response.css('div.price::text').get()
        try:
            square_value_data = response.css('div.info-attr span::text').getall()[3]
            if 'm' in square_value_data:
                square_value = square_value_data
            else:
                square_value = response.css('div.info-attr span::text').getall()[2]
        except IndexError:
            square_value = None
        try:
            date_value = response.css('div.info-attr span::text').getall()[12]
            if date_value.find("/") != -1:
                date = date_value
        except IndexError:
            date_value = response.css('div.info-attr span::text').getall()[10]
            if date_value.find("/") != -1:
                date = date_value
            else:
                date = response.css('div.info-attr span::text').getall()[9]
        if date is not None:
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
            elif "hôm nay" in date:
                date_posting = now_date
                print("days")
            elif "hôm qua" in date:
                difference = timedelta(days=1)
                date_posting = now_date - difference
                print("days")
            elif "tuần" in date:
                day_difference = int(date.split(" ")[0])
                difference = timedelta(days=7)
                date_posting = now_date - difference
                print("week")
            elif "năm" in date:
                day_difference = int(date.split(" ")[0])
                difference = timedelta(days=365)
                date_posting = now_date - difference
                print("year")
            elif "tháng" in date:
                day_difference = int(date.split(" ")[0])
                difference = timedelta(days=30)
                date_posting = now_date - difference
                print("month")
            elif "phút" in date:
                second_difference = int(date.split(" ")[0])
                difference = timedelta(seconds=second_difference)
                date_posting = now_date - difference
                print("phút")
            else:
                date_posting = datetime.strptime(date, "%d/%m/%Y")
                print("other")
        else:
            date_posting = datetime.now().date()
        if self.pass_date is None:
            yield {
                'url': url_value,
                'title': title_value,
                'detail': detail_value,
                'price': price_value,
                'square': square_value,
                'date': date_posting
            }
        elif date_posting >= self.pass_date:
            yield {
                'url': url_value,
                'title': title_value,
                'detail': detail_value,
                'price': price_value,
                'square': square_value,
                'date': date_posting
            }
        else:
             self.stop_extraction = True