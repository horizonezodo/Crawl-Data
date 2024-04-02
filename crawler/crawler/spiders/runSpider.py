import os
import subprocess
import shutil
import sys
import time
def get_spider_name(file_path):
    filename = os.path.basename(file_path)
    spider_name = os.path.splitext(filename)[0]
    return spider_name
def run_commands(latesDate):
    if latesDate == "null":
        commands = [
            "cd /home/dev/Downloads/crawler/venv",
            "source /home/dev/Downloads/crawler/venv/bin/activate",
            "cd /home/dev/Downloads/crawler/crawler/spiders",
            "scrapy crawl test1 --logfile=/home/dev/Downloads/mylog.log"
        ]
    else:
        commands = [
            "cd /home/dev/Downloads/crawler/venv",
            "source /home/dev/Downloads/crawler/venv/bin/activate",
            "cd /home/dev/Downloads/crawler/crawler/spiders",
            "scrapy crawl test1 -a pass_date_str='{}' --logfile=/home/dev/Downloads/mylog.log".format(latesDate)
        ]
    full_command = " && ".join(commands)
    subprocess.call(full_command, shell=True, executable="/bin/bash")
def check_file_exists(file_path):
    return os.path.exists(file_path)
if __name__ == "__main__":
    #source_file = '/home/dev/Desktop/iNhaDatSpider.py.py'
    source_file = sys.argv[1]
    latesDate = sys.argv[2]
    # source_file = "/home/dev/Desktop/otoXeHoiSpider.py"
    # latesDate = "null"
    timedata = latesDate.replace("T", " ")
    print('date python got '+timedata)
    print(type(timedata))
    destination_file = '/home/dev/Downloads/crawler/crawler/spiders/{}'.format(os.path.basename(source_file))
    shutil.copy2(source_file, destination_file)
    run_commands(timedata)
    if check_file_exists(destination_file):
        os.remove(destination_file)

