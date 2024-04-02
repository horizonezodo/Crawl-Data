from scrapy import Spider
from scrapy.selector import Selector
import sys
crawler_module_path = 'E:\\python_leaning\\TestPython\\crawler'
sys.path.append(crawler_module_path)
from crawler.items import CrawlerItem
import spidersConfig

class CrawlerSpider(Spider):
    # name = "crawler"
    # allowed_domains = ["vnexpress.net"]
    # start_urls = [
    #     "https://vnexpress.net/"
    # ]
    #
    # def parse(self, response):
    #     questions = Selector(response).xpath(
    #         '//section[@class="section section_stream_home section_container"]/div/div[@class="col-left col-small"]/article')
    #
    #     for question in questions:
    #         item = CrawlerItem()
    #
    #         item['title'] = question.xpath(
    #             'h3/a/text()').extract_first()
    #         item['description'] = question.xpath(
    #             'p/a/text()').extract_first()
    #         item['sdt'] = question.xpath(
    #             'div[@class="cmt-command"]/span/text()').extract_first()
    #
    #         yield item

    name = f"{spidersConfig.spider_name}"
    allowed_domains = [f"{spidersConfig.domain}"]
    start_urls = [
        spidersConfig.start_url
    ]

    def parse(self, response):

        questions = Selector(response).xpath(f'{spidersConfig.question_xpath}')

        for question in questions:
            item = CrawlerItem()

            item['title'] = question.xpath(
                f'{spidersConfig.title_xpath}').extract_first()
            item['description'] = question.xpath(
                f'{spidersConfig.description_xpath}').extract_first()
            item['sdt'] = question.xpath(
                f'{spidersConfig.sdt_xpath}').extract_first()
            yield item
