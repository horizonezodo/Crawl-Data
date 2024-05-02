from datetime import datetime, timedelta
import os
import scrapy

class BdsSpider(scrapy.Spider):
    name = "nhadat24"
    allowed_domains = ['nhadat24h.net']
    start_urls = ['https://nhadat24h.net/nha-dat-ban-ha-noi']

    custom_settings = {
        'FEEDS': {
            f'{os.getcwd()}/result/{name}.json': {'format': 'json', 'overwrite': True}
        }
    }

    def __init__(self, pass_date_str='', *args, **kwargs):
        super(BdsSpider, self).__init__(*args, **kwargs)
        try:
            self.pass_date = datetime.strptime(pass_date_str, "%d/%m/%Y %H:%M:%S")
        except ValueError:
            self.pass_date = None
        self.stop_extraction = False

    def parse(self, response):
        now_date = datetime.now()
        listbds = response.css('div.dv-body-frm div.pn1')
        for bds in listbds:
            url_value = "https://nhadat24h.net" + bds.css('div.pn1 a::attr(href)').get()
            title_value = bds.css('div.pn1 a::attr(title)').get()
            detail_value = bds.css('div.reviewproperty1 label.lb-des::text').get()
            price_value = bds.css('div.pn1 p label.a-txt-cl1::text').get()
            square_value = bds.css('div.pn1 p label.a-txt-cl2::text').get()
            date = bds.css('div.pn1 p.time::text').get()

            if "lúc" in date and "phút" in date and "giờ" in date:
                    tmp_date = date.split(" ")[0]
                    if "," in tmp_date:
                        tmp_date2 = tmp_date.split(",")
                    tmp_hours = date.split(" ")[2]
                    tmp_secound = date.split(" ")[4]
                    date_string = tmp_date2[0] + " " + tmp_hours + ":" + tmp_secound + ":00"
                    date_format = "%d/%m/%Y %H:%M:%S"
                    date_posting = datetime.strptime(date_string, date_format)
            elif "phút" in date:
                    second_difference = int(date.split(" ")[0])
                    difference = timedelta(seconds=second_difference)
                    date_posting = now_date - difference
            elif "phút" in date and "giờ" in date:
                    hour_difference = int(date.split(" ")[0])
                    second_difference = int(date.split(" ")[2])
                    difference = timedelta(hours=hour_difference, seconds=second_difference)
                    date_posting = now_date - difference
            elif "giờ" in date:
                    hour_difference = int(date.split(" ")[0])
                    second_difference = int(date.split(" ")[2])
                    difference = timedelta(hours=hour_difference, seconds=second_difference)
                    date_posting = now_date - difference
            elif "ngày" in date:
                    day_difference = int(date.split(" ")[0])
                    date_posting = now_date - timedelta(days=day_difference)
            else:
                    date_posting = now_date
            if self.pass_date is None or date_posting > self.pass_date :
                yield {
                    'url': url_value,
                    'title': title_value,
                    'detail': detail_value,
                    'price': price_value,
                    'square': square_value,
                    "date": date_posting
                }
            else:
                self.stop_extraction = True
                break

        if not self.stop_extraction:
            next_page = response.css('div.NaviPage ul li a::attr(href)').getall()[
                len(response.css('div.NaviPage ul li a::attr(href)').getall()) - 1]
            if next_page is not None:
                yield response.follow(next_page, callback=self.parse)

