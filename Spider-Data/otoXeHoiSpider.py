from datetime import datetime, timedelta
import scrapy
import os

class otoXeHoiSpider(scrapy.Spider):
    name = "otoXeHoiSpider"
    allowed_domains = ['otoxehoi.com']
    start_urls = ['https://otoxehoi.com/oto/']

    custom_settings = {
        'FEEDS': {
            f'{os.getcwd()}/result/{name}.json': {'format': 'json', 'overwrite': True}
        }
    }

    def __init__(self, pass_date_str='', *args, **kwargs):
        super(otoXeHoiSpider, self).__init__(*args, **kwargs)
        try:
            self.pass_date = datetime.strptime(pass_date_str, "%d/%m/%Y")
        except ValueError:
            self.pass_date = None
        self.stop_extraction = False
        self.i = 1

    @staticmethod
    def cleanData(String):
        String  = String.replace("\n","")
        String = String.replace("\t","")
        String = String.replace("\r","")
        String = String.strip()
        return String

    def parse(self, response):
        print(response)
        listCar = response.css('div#autoGridViewList div.item')
        print(len(listCar))
        for car in listCar:
            item_url = 'https://otoxehoi.com{}'.format(car.css('div.rowInfo h2 a::attr(href)').get())
            yield response.follow(item_url, callback=self.parse_car_response)
        if not self.stop_extraction:
            next_page = response.css('div.pagingGrid ul.pagination li a[title="Trang kế"]::attr(href)').get()
            if next_page is not None:
                next_page_url = "https://otoxehoi.com"+next_page
                print('next_page_url : {}'.format(next_page_url))
                yield response.follow(next_page_url, callback=self.parse)

    def parse_car_response(self, response):
        print(response)
        now_date = datetime.now().date()
        url_value = ''.join(map(str, response.url))
        title_value = response.css('div.autoTitle h1::text').get()
        price_value = response.css('div.info-auto-header-content span.info-auto-header-right-price::text').get().split(":")[1].strip()
        try:
            gear_value_data = response.css('table.tborder tr td::text').getall()[21]
            if "tự động" in gear_value_data.lower() or "sàn" in gear_value_data.lower():
                gear_value = gear_value_data
            else:
                gear_value = None
        except IndexError:
            gear_value = None
        try:
            tyle_value_data = response.css('table.tborder tr td::text').getall()[15]
            if "sedan" in tyle_value_data.lower() or "hatchback" in tyle_value_data.lower() or "suv" in tyle_value_data.lower() or "crossover" in tyle_value_data.lower() or "couple" in tyle_value_data.lower() or "minivan" in tyle_value_data.lower() or "pickup" in tyle_value_data.lower() or "truck" in tyle_value_data.lower() or "van" in tyle_value_data.lower() or "wagon" in tyle_value_data.lower() or "convertible" in tyle_value_data.lower():
                tyle_value = tyle_value_data
            else:
                tyle_value = None
        except IndexError:
            tyle_value = None
        try:
            date_value = response.css('table.tborder tr td::text').getall()[17].split("-")[0]
        except:
            date_value = datetime.now().date().strftime("%d/%m/%Y")
        detail_value = ''.join(str(e) for e in response.css('tr td[colspan="4"] p::text').getall())
        date = date_value.strip().replace(".","/")
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
        if self.pass_date is None:
            yield {
                'url': url_value,
                'title': self.cleanData(title_value),
                'detail': self.cleanData(detail_value),
                'price': self.cleanData(price_value),
                'gear': self.cleanData(gear_value),
                'type': self.cleanData(tyle_value),
                'date': date_posting
            }
        elif date_posting >= self.pass_date:
            yield {
                'url': url_value,
                'title': self.cleanData(title_value),
                'detail': self.cleanData(detail_value),
                'price': self.cleanData(price_value),
                'gear': self.cleanData(gear_value),
                'type': self.cleanData(tyle_value),
                'date': date_posting
            }
        else:
             self.stop_extraction = True
             return

