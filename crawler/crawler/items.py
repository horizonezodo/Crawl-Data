import scrapy

class CrawlerItem(scrapy.Item):
    # define the fields for your item here like:
    
    title = scrapy.Field()
    description = scrapy.Field()
    sdt = scrapy.Field()