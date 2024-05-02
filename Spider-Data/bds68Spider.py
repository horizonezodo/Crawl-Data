from datetime import datetime, timedelta
import os
import scrapy


class bds68Spider(scrapy.Spider):
    name = "bds68Spider"
    allowed_domains = ['bds68.com.vn']
    start_urls = ['https://bds68.com.vn/nha-dat-ban/ha-noi']

    custom_settings = {
        'FEEDS': {
            f'{os.getcwd()}/result/{name}.json': {'format': 'json', 'overwrite': True}
        }
    }

    def __init__(self, pass_date_str='', *args, **kwargs):
        super(bds68Spider, self).__init__(*args, **kwargs)
        try:
            self.pass_date = datetime.strptime(pass_date_str, "%Y-%m-%d %H:%M:%S")
        except ValueError:
            self.pass_date = None
        self.stop_extraction = False
    webPageValue = 1
    def parse(self, response):
        listbds = response.css('div#ser-grid-detail div div.prop-box-item-contain')
        for bds in listbds:
            url_value = "https://bds68.com.vn" + bds.css('div.div_prop_info div.header-prop-title h4 a::attr(href)').get()
            yield response.follow(url_value, callback=self.parse_bds_response)
        if not self.stop_extraction:
            print(self.webPageValue)
            self.webPageValue = self.webPageValue + 1
            if self.webPageValue <= 2332:
                print('next page')
                print(self.webPageValue)
                next_page = 'https://bds68.com.vn/nha-dat-ban/ha-noi?pg={}'.format(self.webPageValue)
                print('url value: ', next_page)
                yield response.follow(next_page, callback=self.parse)

    def parse_bds_response(self, response):
        print(response)
        now_date = datetime.now().date()
        url_value_data = ''.join(map(str, response.url))
        url_value = ''.join(map(str, url_value_data))
        title_value = response.css('div.body-content h1::text').get()
        price_value_data = response.css('div.big-prop-price::text').get()
        if price_value_data is not None:
            price_value = price_value_data.split(":")[1].strip()
        detail_value = ''.join(map(str,response.css('div.readmore-box p::text').getall()))
        square_value = response.xpath('/html/body/div[3]/div[1]/div[1]/div[1]/div[2]/div[1]/div[4]/span/text()').get()
        date_value = response.css('div.date-create span::text').get()
        if date_value is not None:
            date = date_value.split(":")[1].strip()
        else:
            date_value = now_date
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
            # time_str = "12:00PM"
            # time_obj = datetime.strptime(time_str, "%I:%M%p").time()
            # date_obj = datetime.strptime(date, "%d/%m/%Y")
            # date_get = datetime.combine(date_obj, time_obj)
            #date_posting_str = date_get.strftime('%Y/%m/%d %H:%M')
            date_posting = datetime.strptime(date, "%d/%m/%Y")
        if self.pass_date is None:
            print("pass date is none")
            yield {
                'url': url_value,
                'title': title_value,
                'detail': detail_value,
                'price': price_value,
                'square': square_value,
                'date': date_posting
            }
        elif date_value >= self.pass_date:
            print(date_value >= self.pass_date)
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
             return