from datetime import datetime, timedelta
import scrapy
import os
class vuCarSpider(scrapy.Spider):
    name = "vuCarSpider"
    allowed_domains = ['vucar.vn']
    start_urls = ['https://vucar.vn/xe?page=1&per_page=72']

    custom_settings = {
        'FEEDS': {
            f'{os.getcwd()}/result/{name}.json': {'format': 'json', 'overwrite': True}
        }
    }

    def __init__(self, pass_date_str='', *args, **kwargs):
        super(vuCarSpider, self).__init__(*args, **kwargs)
        try:
            self.pass_date = datetime.strptime(pass_date_str, "%d/%m/%Y")
        except ValueError:
            self.pass_date = None
        self.stop_extraction = False
        self.i = 1

    def parse(self, response):
        listCar = response.css('div.mx-auto a')

        for car in listCar:
            item_url = 'https://vucar.vn{}'.format(car.css('a.border-neutral-3::attr(href)').get())
            yield response.follow(item_url, callback=self.parse_car_response)
        if not self.stop_extraction:
            self.i += 1
            if self.i < 91:
                next_page = "https://vucar.vn/xe?page={}&per_page=8".format(self.i)
                yield response.follow(next_page, callback=self.parse)

    def parse_car_response(self, response):
        url_value_data = ''.join(map(str, response.url))
        url_value = ''.join(map(str, url_value_data))
        title_value = response.css('div.p-4 h1.text-gray-700::text').get()
        price_value = response.css('div.gap-2 p.line-clamp-1::text').get()
        try:
            gear_value_data = response.css('td p.text-base::text').getall()[5]
            if "tự động" in gear_value_data.lower() or "sàn" in gear_value_data.lower():
                gear_value = gear_value_data
            else:
                gear_value = None
        except IndexError:
            gear_value = None
        try:
            tyle_value_data = response.css('td p.text-base::text').getall()[9]
            if 'mpv' in tyle_value_data.lower() or "cuv" in tyle_value_data.lower() or "sedan" in tyle_value_data.lower() or "hatchback" in tyle_value_data.lower() or "suv" in tyle_value_data.lower() or "crossover" in tyle_value_data.lower() or "couple" in tyle_value_data.lower() or "minivan" in tyle_value_data.lower() or "pickup" in tyle_value_data.lower() or "truck" in tyle_value_data.lower() or "van" in tyle_value_data.lower() or "wagon" in tyle_value_data.lower() or "convertible" in tyle_value_data.lower():
                tyle_value = tyle_value_data
            else:
                tyle_value = None
        except IndexError:
            tyle_value = None
        date_value = datetime.now().date()
        detail_value_data = (''.join(str(e) for e in response.css('tr td p::text').getall())),
        detail_value = ''.join(map(str, detail_value_data))
        # date = date_value.strip()
        # if "giờ" in date:
        #     hour_difference = int(date.split(" ")[0])
        #     difference = timedelta(hours=hour_difference)
        #     date_posting = now_date - difference
        #     print("hours")
        # elif "ngày" in date:
        #     day_difference = int(date.split(" ")[0])
        #     difference = timedelta(days=day_difference)
        #     date_posting = now_date - difference
        #     print("days")
        # elif "hôm nay" in date:
        #     date_posting = now_date
        #     print("days")
        # elif "hôm qua" in date:
        #     difference = timedelta(days=1)
        #     date_posting = now_date - difference
        #     print("days")
        # elif "tuần" in date:
        #     day_difference = int(date.split(" ")[0])
        #     difference = timedelta(days=7)
        #     date_posting = now_date - difference
        #     print("week")
        # elif "năm" in date:
        #     day_difference = int(date.split(" ")[0])
        #     difference = timedelta(days=365)
        #     date_posting = now_date - difference
        #     print("year")
        # elif "tháng" in date:
        #     day_difference = int(date.split(" ")[0])
        #     difference = timedelta(days=30)
        #     date_posting = now_date - difference
        #     print("month")
        # elif "phút" in date:
        #     second_difference = int(date.split(" ")[0])
        #     difference = timedelta(seconds=second_difference)
        #     date_posting = now_date - difference
        #     print("phút")
        # else:
        #     date_posting = datetime.strptime(date, "%d/%m/%Y")
        #     print("other")
        if self.pass_date is None:
            yield {
                'url': url_value,
                'title': title_value,
                'detail': detail_value,
                'price': price_value,
                'gear': gear_value,
                'type': tyle_value,
                'date': date_value
            }
        elif date_value >= self.pass_date:
            yield {
                'url': url_value,
                'title': title_value,
                'detail': detail_value,
                'price': price_value,
                'gear': gear_value,
                'type': tyle_value,
                'date': date_value
            }
        else:
             self.stop_extraction = True
             return

