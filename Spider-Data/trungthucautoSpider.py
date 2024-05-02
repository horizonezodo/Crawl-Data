from datetime import datetime, timedelta
import scrapy
import os
class trungthucautoSpider(scrapy.Spider):
    name = "trungthucautoSpider"
    allowed_domains = ['trungthucauto.vn']
    start_urls = ['https://trungthucauto.vn/tim-xe/?tinh-trang=used']

    custom_settings = {
        'FEEDS': {
           f'{os.getcwd()}/result/{name}.json': {'format': 'json', 'overwrite': True}
        }
    }

    def __init__(self, pass_date_str='', *args, **kwargs):
        super(trungthucautoSpider, self).__init__(*args, **kwargs)
        try:
            self.pass_date = datetime.strptime(pass_date_str, "%d/%m/%Y")
        except ValueError:
            self.pass_date = None
        self.stop_extraction = False
        self.i = 1

    @staticmethod
    def cleanData(String):
        String = String.replace("\n", "")
        String = String.replace("\t", "")
        String = String.replace("\r", "")
        String = String.strip()
        return String

    def parse(self, response):
        listCar = response.css('div.vehica-inventory-v1__results div.vehica-inventory-v1__results__card')

        for car in listCar:
            item_url = car.css('div.vehica-car-card__inner a.vehica-car-card-link::attr(href)').get()
            yield response.follow(item_url, callback=self.parse_car_response)
        if not self.stop_extraction:
            try:
                self.i += 1
                if self.i < 23:
                    next_page = "https://trungthucauto.vn/tim-xe/?tinh-trang=used&trang-hientai={}&sapxep-theo=moi-nhat".format(self.i)
                    yield response.follow(next_page, callback=self.parse)
            except ValueError:
                print("End of list page navigation")

    def parse_car_response(self, response):

        url_value = (''.join(str(e) for e in response.url)).strip()
        title_value = response.css('div.elementor-widget-container div.vehica-car-name::text').get()
        price_value = response.css('div.elementor-widget-container div.vehica-car-price::text').get()
        try:
            gear_value_data = response.css('div.vehica-grid div.vehica-car-attributes__values::text').getall()[4]
            if "tự động" in gear_value_data.lower() or "sàn" in gear_value_data.lower():
                gear_value = gear_value_data
            else:
                gear_value = None
        except IndexError:
            gear_value = None
        tyle_value_data = response.css('div.vehica-car-features a::attr(title)').get()
        if "sedan" in tyle_value_data.lower() or "hatchback" in tyle_value_data.lower() or "suv" in tyle_value_data.lower() or "crossover" in tyle_value_data.lower() or "couple" in tyle_value_data.lower() or "minivan" in tyle_value_data.lower() or "pickup" in tyle_value_data.lower() or "truck" in tyle_value_data.lower() or "van" in tyle_value_data.lower() or "wagon" in tyle_value_data.lower() or "convertible" in tyle_value_data.lower():
            tyle_value = tyle_value_data
        else:
            tyle_value = None
        #date_value = response.css('div.vehica-car-feature span::text').get()
        detail_value = (' '.join(str(e) for e in response.css('div.elementor-widget-container ol li strong::text').getall())).strip()
        now_date = datetime.now().date()
        difference = timedelta(days=(self.i - 1))
        date_posting = now_date - difference
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
