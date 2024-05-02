import os
from datetime import datetime, timedelta
import os
import scrapy


class CenhomesSpider(scrapy.Spider):
    name = "cenhomes"
    allowed_domains = ['cenhomes.vn']
    start_urls = ['https://cenhomes.vn/mua-nha/ha-noi']

    custom_settings = {
        'FEEDS': {
            f'{os.getcwd()}/result/{name}.json': {'format': 'json', 'overwrite': True}
        }
    }

    def __init__(self, pass_date_str='', *args, **kwargs):
        super(CenhomesSpider, self).__init__(*args, **kwargs)
        try:
            self.pass_date = datetime.strptime(pass_date_str, "%Y-%m-%d %H:%M:%S")
        except ValueError:
            self.pass_date = None
        self.stop_extraction = False

    def parse(self, response):
        print("pass-date: " ,self.pass_date)

        listbds = response.css('div.container div.b__mainProduct--items')
        for bds in listbds:
            url_value = bds.css('div.b__mainProduct--desc a::attr(href)').get()
            price_data = bds.css('p.b__price::text').get()
            date = bds.css('div.b__tags--items label.b__time::text').get()
            yield response.follow(url_value, callback=self.parse_bds_response, meta={'date_data': date, 'price_data': price_data})

        if not self.stop_extraction:
            next_page = response.css('ul.pagination li.page-item a::attr(href)').getall()[
                len(response.css('ul.pagination li.page-item a::attr(href)').getall()) - 1]
            if next_page is not None:
                yield response.follow(next_page, callback=self.parse)

    def parse_bds_response(self, response):
        now_date = datetime.now()
        url_value = ''.join(map(str, response.url))
        title_value = response.css('h1.page-title::text').get()
        price_value_data = response.meta.get("price_data")
        if price_value_data is not None:
            price_value = price_value_data.split("-")[0]
        square_value = response.css('span.icon-area::text').get()
        date = response.meta.get("date_data")
        print(date)
        detail_value = (' '.join(str(e) for e in response.css('div.description::text').getall())).strip()
        if detail_value is None or detail_value.isspace():
            detail_value = (' '.join(str(e) for e in response.css('div.wrap-description p::text').getall())).strip()
        if "hours" in date or "hour" in date:
            hour_difference = int(date.split(" ")[0])
            difference = timedelta(hours=hour_difference)
            date_posting = now_date - difference
            print("hours")
        elif "days" in date or "day" in date:
            day_difference = int(date.split(" ")[0])
            difference = timedelta(days=day_difference)
            date_posting = now_date - difference
            print("days")
        elif "tuần" in date or "week" in date or "weeks" in date:
            week_difference = int(date.split(" ")[0])
            difference = timedelta(weeks=week_difference)
            date_posting = now_date - difference
            print("weeks")
        elif "tháng" in date or "month" in date or "months" in date:
            month_difference = int(date.split(" ")[0])
            difference = timedelta(days=month_difference * 30)
            date_posting = now_date - difference
            print("months")
        elif "năm" in date or "years" in date or "year" in date:
            years_difference = int(date.split(" ")[0])
            difference = timedelta(days=years_difference * 365)
            date_posting = now_date - difference

        if (self.pass_date is None) or (date_posting > self.pass_date):
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