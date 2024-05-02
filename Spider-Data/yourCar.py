from datetime import datetime, timedelta
import scrapy
import os
class yourCarSpider(scrapy.Spider):
    name = "yourCar"
    allowed_domains = ['youcar.vn']
    start_urls = ['https://youcar.vn/oto?page=1']

    custom_settings = {
        'FEEDS': {
            f'{os.getcwd()}/result/{name}.json': {'format': 'json', 'overwrite': True}
        }

    }

    def __init__(self, pass_date_str='', *args, **kwargs):
        super(yourCarSpider, self).__init__(*args, **kwargs)
        try:
            self.pass_date = datetime.strptime(pass_date_str, "%d/%m/%Y")
        except ValueError:
            self.pass_date = None
        self.stop_extraction = False
        self.i = 1

    @staticmethod
    def getPrice(currency_string):
        currency_number = int(currency_string.replace(".", "").replace(" đ", ""))
        if currency_number < 1000000000:
            phan_nguyen = currency_number // 1000000
            if currency_number % 1000000 == 0:
                return str(phan_nguyen) + " triệu"
            else:
                phan_du = (currency_number - phan_nguyen * 1000000) // 100000
                return str(phan_nguyen) + "." + str(phan_du) + " triệu"
        else:
            trieu_part = currency_number // 1000000000
            if currency_number % 1000000000 == 0:
                return str(trieu_part) + " tỷ"
            else:
                ty_part = (currency_number - trieu_part * 1000000000) // 1000000
                return str(trieu_part) + "." + str(ty_part) + " tỷ"

    def parse(self, response):
        listCar = response.css('div.cars div.car-item')

        for car in listCar:
            item_url = car.css('div.car-sell-info-wapper a.no-ul::attr(href)').get()
            yield response.follow(item_url, callback=self.parse_car_response)
        if not self.stop_extraction:
            try:
                list_page = response.css('ul.pagination li a::attr(href)').getall()
                self.i += 1
                next_page = "https://youcar.vn/oto?page={}".format(self.i)
                if next_page in list_page:
                    yield response.follow(next_page, callback=self.parse)
            except ValueError:
                print("End of list page navigation")

    def parse_car_response(self, response):
        now_date = datetime.now().date()
        url_value = ''.join(map(str,response.url))
        title_value = response.css('div.main-info-head h1::text').get()
        price_value_data = response.css('div.price::text').get()
        price_value = self.getPrice(price_value_data)
        gear_value = None
        tyle_value = None
        date_value = response.css('div.main-info-head label::text').get()
        detail_value = response.css('div.car-desc p::text').get()
        date = date_value.strip()
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
                difference = timedelta(days=7 * day_difference)
                date_posting = now_date - difference
                print("week")
            elif "năm" in date:
                day_difference = int(date.split(" ")[0])
                difference = timedelta(days=365 * day_difference)
                date_posting = now_date - difference
                print("year")
            elif "tháng" in date:
                day_difference = int(date.split(" ")[0])
                difference = timedelta(days=30 * day_difference)
                date_posting = now_date - difference
                print("month")
            elif "phút" in date:
                second_difference = int(date.split(" ")[0])
                difference = timedelta(seconds=second_difference)
                date_posting = now_date - difference
                print("phút")
            elif date is None:
                date_posting_value = datetime.now().date()
                date_posting = datetime.strftime(date_posting_value, "%d/%m/%Y")
                print("date is none")
            else:
                date = date.split(" ")[2]
                date_posting = datetime.strptime(date, "%d/%m/%Y").date()
                print("other")
        else:
            date_posting = datetime.now().date().strftime('%d/%m/%Y')
        if self.pass_date is None:
            yield {
                'url': url_value,
                'title': title_value,
                'detail': detail_value,
                'price': price_value,
                'gear': gear_value,
                'type': tyle_value,
                'date': date_posting
            }
        elif date_posting >= self.pass_date:
            yield {
                'url': url_value,
                'title': title_value,
                'detail': detail_value,
                'price':price_value,
                'gear': gear_value,
                'type': tyle_value,
                'date': date_posting
            }
        else:
             self.stop_extraction = True