import re
from scrapy.crawler import CrawlerProcess
import json
import os
import sys
import ast
import spidersConfig

sys.path.append('E:\\python_leaning\\TestPython\\crawler')
from crawlerData import CrawlerSpider

def extract_parse_function(file_path):
    with open(file_path,'r') as file:
        script_content = file.read()
        tree = ast.parse(script_content)

        for node in ast.walk(tree):
            if isinstance(node, ast.FunctionDef) and node.name=='parse':
                start_lineno, end_lineno = node.lineno, node.end_lineno
                parse_function_content = '\n'.join(script_content.splitlines()[start_lineno - 1:end_lineno])
                return parse_function_content

def extract_xpath_from_parse_function(file_path):
    with open(file_path,'r') as file:
        script_content = file.read()
        tree = ast.parse(script_content)

        xpaths = []
        for node in ast.walk(tree):
            if isinstance(node, ast.Call) and hasattr(node.func, 'attr') and node.func.attr == 'xpath':
                xpath = node.args[0].s
                xpaths.append(xpath)

        return xpaths

def run_crawler():
    process = CrawlerProcess({
        'USER_AGENT': 'Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)',
        'FEED_FORMAT': 'json',  # Định dạng xuất
        'FEED_URI': 'E:\\python_leaning\\TestPython\\crawler\\crawler\\spiders\\output.json'  # Đường dẫn tệp xuất
    })
    process.crawl(CrawlerSpider)
    process.start()

def load_json_with_unicode(file_path):
    with open(file_path, 'r', encoding='utf-8') as json_file:
        data = json_file.read()
        return json.loads(data)

def dump_json(data, file_path):
    with open(file_path, 'w', encoding='utf-8') as json_file:
        json.dump(data, json_file, ensure_ascii=False, indent=2)

def check_file_exists(file_path):
    return os.path.exists(file_path)

def get_website_detail(file_path):
    with open(file_path, 'r') as f:
        content = f.read()
    name = re.search(r'name = "(.*?)"', content)
    allowed_domains = re.search(r'allowed_domains = \["(.*?)"\]', content)
    start_urls = re.findall(r'start_urls = \[\s*"(.*?)"\s*\]', content, re.DOTALL)
    name_spider = name.group(1)
    domains_spider = allowed_domains.group(1)

    yield name_spider
    yield domains_spider
    yield start_urls

def clean_and_insert(file_path,parse_code):
    with open(file_path,'r') as file:
        lines = file.readlines()
    start_index = None
    end_index = None
    for i, line in enumerate(lines):
        if 'def parse(self, response):' in line:
            start_index = i
        elif start_index is not None and line.strip() == '':
            end_index = i
    if start_index is not None and (end_index is not None or end_index == len(lines) - 1):
        del lines[start_index + 1:end_index]
        lines.insert(start_index + 1, parse_code)
        with open(file_path, 'w') as file:
            file.writelines(lines)
    else:
        print("Không tìm thấy hàm parse trong file.")

if __name__ == "__main__":

    script_file_path = sys.argv[1] if len(sys.argv) > 1 else ''
    #script_file_path='E:\\Crawler\\uploads\\alonhadat_spider.py'
    #script_file_path = 'E:\\Crawler\\uploads\\testCrawler.py'
    name,allowed_domains,start_urls = get_website_detail(script_file_path);

    xpaths = extract_xpath_from_parse_function(script_file_path)
    question_xpath = xpaths[0]
    title_xpath = xpaths[1]
    description_xpath = xpaths[2]
    sdt_xpath = xpaths[3]

    CrawlerSpider.start_urls = start_urls
    spidersConfig.spider_name=name
    spidersConfig.domain = allowed_domains
    spidersConfig.start_url = start_urls
    spidersConfig.question_xpath = question_xpath
    spidersConfig.title_xpath = title_xpath
    spidersConfig.description_xpath = description_xpath
    spidersConfig.sdt_xpath = sdt_xpath

    # print(f"name {spidersConfig.spider_name}")
    # print(f"allowed_domains {spidersConfig.domain}")
    # print(f"start_urls {spidersConfig.start_url}")
    # print(f"question_xpath {spidersConfig.question_xpath}")
    # print(f"title_xpath {spidersConfig.title_xpath}")
    # print(f"description_xpath {spidersConfig.description_xpath}")
    # print(f"sdt_xpath {spidersConfig.sdt_xpath}")

    if check_file_exists('E:\\python_leaning\\TestPython\\crawler\\crawler\\spiders\\output.json'):
        os.remove('E:\\python_leaning\\TestPython\\crawler\\crawler\\spiders\\output.json')

    run_crawler()
    json_file_path = 'E:\\python_leaning\\TestPython\\crawler\\crawler\\spiders\\output.json'
    data = load_json_with_unicode(json_file_path)
    output_file_path = 'E:\\Crawler\\data\\encode.json'
    dump_json(data, output_file_path)
