from datetime import datetime, timedelta
import os
import scrapy


class NhadatCafelandSpider(scrapy.Spider):
    name = "nhadatCafelandSpider"
    allowed_domains = ['nhadat.cafeland.vn']
    start_urls = ['https://nhadat.cafeland.vn/nha-dat-ban-tai-tp-ho-chi-minh/']

    custom_settings = {
        'FEEDS': {
            f'{os.getcwd()}/result/{name}.json': {'format': 'json', 'overwrite': True}
        }
    }

    def __init__(self, pass_date_str='', *args, **kwargs):
        super(NhadatCafelandSpider, self).__init__(*args, **kwargs)
        try:
            self.pass_date = datetime.strptime(pass_date_str, "%Y-%m-%d %H:%M:%S")
        except ValueError:
            self.pass_date = None
        self.stop_extraction = False

    def parse(self, response):
        print(response)
        now_date = datetime.now()
        listbds = response.css('div.property-list div.row-item')
        for bds in listbds:
            url_value = bds.css('div.info-real div.reales-title a::attr(href)').get()
            title_value = bds.css('div.info-real div.reales-title a::text').get()
            detail_value = bds.css('div.info-real div.reales-preview::text').get()
            price_value_data = bds.css('span.reales-price::text').get()
            if price_value_data is not None:
                price_value = price_value_data
            else:
                price_value = None

            square_value_data = bds.css('span.reales-area::text').get()
            if square_value_data is not None:
                square_value = square_value_data.strip().split(" ")[1]
            else:
                square_value = None
            try:
                date_value = response.css('div.member-info div.info div.reals-update-time::text').get()
                if date_value is not None:
                    date = date_value.split(":")[1].strip()
                else:
                    date = None
            except IndexError:
                date = datetime.now().date()
            if "Hôm nay" in date:
                date_posting = now_date
                print("Hôm nay")
            elif "Hôm qua" in date:
                difference = timedelta(days=1)
                date_posting = now_date - difference
                print("day")
            elif "ngày" in date:
                day_difference = int(date.split(" ")[0])
                difference = timedelta(days=day_difference)
                date_posting = now_date - difference
                print("ngày")
            elif "tuần" in date:
                week_difference = int(date.split(" ")[0])
                difference = timedelta(weeks=week_difference)
                date_posting = now_date - difference
                print("tuần")
            elif "tháng" in date:
                month_difference = int(date.split(" ")[0])
                difference = timedelta(days=month_difference * 30)
                date_posting = now_date - difference
                print("tháng")

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
                break;
        if not self.stop_extraction:
            next_page = response.css('ul.pagination li a::attr(href)').getall()[
                len(response.css('ul.pagination li a::attr(href)').getall()) - 1]
            if next_page is not None:
                yield response.follow(next_page, callback=self.parse)
