from datetime import datetime, timedelta
import os
import scrapy


class nhaDatTop1Spider(scrapy.Spider):
    name = "nhaDatTop1Spider"
    allowed_domains = ['nhadattop1.com']
    start_urls = ['https://nhadattop1.com/ban-nha-rieng-pc2.htm']

    custom_settings = {
        'FEEDS': {
            f'{os.getcwd()}/result/{name}.json': {'format': 'json', 'overwrite': True}
        }
    }

    def __init__(self, pass_date_str='', *args, **kwargs):
        super(nhaDatTop1Spider, self).__init__(*args, **kwargs)
        try:
            self.pass_date = datetime.strptime(pass_date_str, "%d/%m/%Y")
        except ValueError:
            self.pass_date = None
        self.stop_extraction = False
        self.i = 1

    def parse(self, response):
        now_date = datetime.now()
        listbds = response.css('ul.list_pro li.pro_item')

        for bds in listbds:
            url_value ="https://nhadattop1.com" + bds.css('h3.title a::attr(href)').get()
            title_value = bds.css('h3.title a::text').get()
            detail_value = bds.css('div.intro::text').get()
            price_value = bds.css('div.info p.info2 span.info2_lable3::text').get()
            square_value = bds.css('div.group2 p.info3 span.info2_lable3::text').get()
            date = bds.css('p.info4 span.time::text').get()
            date_posting = datetime.strptime(date, "%d/%m/%Y")
            print(date_posting)
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

        if not self.stop_extraction:
            list_page = response.css('div.paging_content a.item_paging::text').getall()
            self.i += 1
            try:
                current_index = list_page.index(str(self.i))
                next_page_url = "https://nhadattop1.com/ban-nha-rieng-pc2/trang-{}".format(self.i)
                yield response.follow(next_page_url, callback=self.parse)
            except ValueError:
                print('index is not in list')
