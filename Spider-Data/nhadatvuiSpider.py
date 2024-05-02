from datetime import datetime, timedelta
import os
import scrapy


class nhadatvuiSpider(scrapy.Spider):
    name = "nhadatvuiSpider"
    allowed_domains = ['nhadatvui.vn']
    start_urls = ['https://nhadatvui.vn/mua-ban-nha-dat']

    custom_settings = {
        'FEEDS': {
            f'{os.getcwd()}/result/{name}.json': {'format': 'json', 'overwrite': True}
        }
    }

    def __init__(self, pass_date_str='', *args, **kwargs):
        super(nhadatvuiSpider, self).__init__(*args, **kwargs)
        try:
            self.pass_date = datetime.strptime(pass_date_str, "%d/%m/%Y")
        except ValueError:
            self.pass_date = None
        self.stop_extraction = False

    def parse(self, response):
        listbds = response.css('div.box-container-inner div.box-item')
        for bds in listbds:
            url_value = bds.css('div.box-item a::attr(href)').get()
            yield response.follow(url_value, callback=self.parse_bds_response)
        if not self.stop_extraction:
            next_page = response.css('ul.pagination li.page-item a[rel="next"]::attr(href)').get()
            if next_page is not None:
                yield response.follow(next_page, callback=self.parse)
    def parse_bds_response(self, response):
        print(response)
        now_date = datetime.now().date()
        url_value_data = ''.join(map(str, response.url))
        url_value = ''.join(map(str, url_value_data))
        title_value = response.css('div.left-title-price h1.line-36::text').get()
        price_value = response.css('div.price-box span.price::text').get()
        detail_value = ''.join(map(str, response.css('div#content-tab-custom p::text').getall()))
        try:
            square_value = response.css('li.border-l::text').getall()[1]
        except IndexError:
            square_value = None
        try:
            date = response.css('div.gap-2 span::text').getall()[1]
        except IndexError:
            date = None

        if date is not None:
            if "Hôm nay" in date:
                time_str = date.split(" ")[2]
                time_obj = datetime.strptime(time_str, "%I:%M%p").time()
                date_obj = datetime.now().date()
                date_get = datetime.combine(date_obj, time_obj)
                date_posting = date_get.strftime('%d/%m/%Y %H:%M')
                print("Hôm nay")
            elif "Hôm qua" in date:
                time_str = date.split(" ")[2]
                time_obj = datetime.strptime(time_str, "%I:%M%p").time()
                difference = timedelta(days=1)
                date_obj = datetime.now().date() - difference
                date_get = datetime.combine(date_obj, time_obj)
                date_posting = date_get.strftime('%d/%m/%Y %H:%M')
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
            elif "giờ" in date:
                hour_difference = int(date.split(" ")[0])
                difference = timedelta(hours=hour_difference)
                date_posting = now_date - difference
            elif "phút" in date:
                second_difference = int(date.split(" ")[0])
                difference = timedelta(seconds=second_difference)
                date_posting = now_date - difference
            elif "tháng" in date:
                month_difference = int(date.split(" ")[0])
                difference = timedelta(days=month_difference * 30)
                date_posting = now_date - difference
                print("tháng")
            else:
                date_posting = datetime.strptime(date, "%d/%m/%Y")
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