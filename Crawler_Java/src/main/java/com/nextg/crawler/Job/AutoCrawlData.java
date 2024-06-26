package com.nextg.crawler.Job;

import com.nextg.crawler.configuration.AppConfig;
import com.nextg.crawler.model.CarDescription;
import com.nextg.crawler.model.StockDescription;
import com.nextg.crawler.model.Website;

import com.nextg.crawler.model.WebsiteDescription;
import com.nextg.crawler.repo.CarDescriptionRepository;
import com.nextg.crawler.repo.StockDescriptionRepository;
import com.nextg.crawler.repo.WebsiteDescriptionRepository;
import com.nextg.crawler.repo.WebsiteRepository;
import com.nextg.crawler.service.deleteDuplicateRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Component
@Slf4j
public class AutoCrawlData {
    @Autowired
    WebsiteRepository webRepo;

    @Autowired
    WebsiteDescriptionRepository dataRepo;

    @Autowired
    CarDescriptionRepository carRepo;

    @Autowired
    StockDescriptionRepository stockRepo;

    @Autowired
    deleteDuplicateRecordService deleteDuplicateRecordService;

    @Scheduled(cron = "0 0 */4 * * *")
    public void tess(){
        log.info("++++++++ok+++++++++++");
    }

    @Scheduled(cron = "0 0 */4 * * *")
    public void autoCrawl() {
        log.info("Run Job");
        try {
            List<Website> li = webRepo.findAll();
            for (Website website : li) {
                String script_load_data_file_path = website.getSpider_url();
                log.info(script_load_data_file_path);
                if (script_load_data_file_path.endsWith(".py")) {
                    String latestDate = getLatestDate(website.getType(), website.getWebsite_url());
                    latestDate = latestDate.replace(" ", "T");
                    log.info(latestDate);
                    String[] command = {
                            "/bin/bash",
                            "-c",
                            "cd "+AppConfig.scrapyWorkDir+" && source "+AppConfig.pythonEnviromentFolder+"/bin/activate && scrapy runspider " + website.getSpider_url() + " -a pass_date_str='" + latestDate + "' --logfile="+AppConfig.scrapyWorkDir+"/"+getOutputFile(website.getSpider_url())+".txt"};
                    Process process = Runtime.getRuntime().exec(command);
                    int exitCode = process.waitFor();
                    log.info(String.valueOf(exitCode));

                    if (exitCode == 0) {
                        InputStream inputStream = process.getInputStream();
                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            System.out.println(line);
                            log.info(line);
                        }
                        String outputFile = AppConfig.resultFolderWorkDir+getOutputFile(website.getSpider_url())+".json";

                        if(website.getType().equalsIgnoreCase("bds")){
                            ObjectMapper objectMapper = new ObjectMapper();
                            log.info("Transfer Data to website descriptions");
                            List<WebsiteDescription> websites = objectMapper.readValue(new File(outputFile),
                                    objectMapper.getTypeFactory().constructCollectionType(List.class, WebsiteDescription.class));
                            dataRepo.saveAll(websites);
                            List<WebsiteDescription> emptyPriceValue =  dataRepo.findAllByContainingPrice("thoa thuan");
                            dataRepo.deleteAll(emptyPriceValue);
                            deleteDuplicateRecordService.deleteWebsiteDuplicateRecords();

                        }
                        else if(website.getType().equalsIgnoreCase("auto")){
                            ObjectMapper objectMapper = new ObjectMapper();
                            log.info("Transfer Data to car descriptions");
                            List<CarDescription> cars = objectMapper.readValue(new File(outputFile),
                                    objectMapper.getTypeFactory().constructCollectionType(List.class, CarDescription.class));
                            carRepo.saveAll(cars);
                            List<CarDescription> emptyPriceValue = carRepo.findAllByContainingPrice("thoa thuan");
                            carRepo.deleteAll(emptyPriceValue);
                            deleteDuplicateRecordService.deleteCarDuplicateRecords();

                        }
                        else if(website.getType().equalsIgnoreCase("stock")){
                            ObjectMapper objectMapper = new ObjectMapper();
                            log.info("Transfer Data to stock descriptions");
                            List<StockDescription> stocks = objectMapper.readValue(new File(outputFile),
                                    objectMapper.getTypeFactory().constructCollectionType(List.class, StockDescription.class));
                            stockRepo.saveAll(stocks);
                        }
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public String getOutputFile(String spiderUrl){
        File file = new File(spiderUrl);
        String filename = file.getName();

        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex != -1){
            filename = filename.substring(0, dotIndex);
        }
        return filename;
    }

    public String getLatestDate(String type, String url) throws IOException {
        if(type.equalsIgnoreCase("bds")){
            return getWebsitelatesDate(url);
        }else if (type.equalsIgnoreCase("auto")){
            return getCarlatesDate(url);
        }
        return null;
    }

    public String getCarlatesDate(String url) throws IOException {

        List<CarDescription> unsortList = getAllCarDescriptionWithUrl(url);
        if (!unsortList.isEmpty()){
            Collections.sort(unsortList, new Comparator<CarDescription>() {
                @Override
                public int compare(CarDescription o1, CarDescription o2) {
                    return o1.getDate().compareTo(o2.getDate());
                }
            });
            return unsortList.get(unsortList.size() -1).getDate();
        }
        else return null;
    }

    public String getWebsitelatesDate(String url) throws IOException {

        List<WebsiteDescription> unsortList = getAllWebsiteDescriptionWithUrl(url);
        if (unsortList.size() > 0){
            Collections.sort(unsortList, new Comparator<WebsiteDescription>() {
                @Override
                public int compare(WebsiteDescription o1, WebsiteDescription o2) {
                    return o1.getDate().compareTo(o2.getDate());
                }
            });
            return unsortList.get(unsortList.size() -1).getDate();
        }
        else return null;
    }

    public List<WebsiteDescription> getAllWebsiteDescriptionWithUrl(String url){
        List<WebsiteDescription> li = dataRepo.findAllWebsiteContainingUrl(url);
        return li;
    }

    public List<CarDescription> getAllCarDescriptionWithUrl(String url){
        List<CarDescription> li = carRepo.findAllByContainingUrl(url);
        return li;
    }
}
