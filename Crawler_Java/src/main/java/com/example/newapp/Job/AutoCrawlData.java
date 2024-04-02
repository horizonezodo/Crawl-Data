package com.example.newapp.Job;

import com.example.newapp.model.Website;

import com.example.newapp.model.WebsiteDescription;
import com.example.newapp.repo.WebsiteDescriptionRepository;
import com.example.newapp.repo.WebsiteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
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

    //@Scheduled(cron = "0 0 */12 * * *")
    public void tess(){
        log.info("++++++++ok+++++++++++");
    }

    //@Scheduled(cron = "0 0 */12 * * *")
    public void autoCrawl() {
        log.info("Run Job");
        try {
            List<Website> li = webRepo.findAll();
            for (Website website : li) {
                String script_load_data_file_path = website.getSpider_url();
                log.info(script_load_data_file_path);
                if (script_load_data_file_path.endsWith(".py")) {
                    String latestDate = getlatesDate(website.getWebsite_url());
                    latestDate = latestDate.replace(" ", "T");
                    log.info(latestDate);
                    Process process = Runtime.getRuntime().exec("python /home/dev/Downloads/crawler/crawler/spiders/runMethod.py " + script_load_data_file_path + " " + latestDate);
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

                        ObjectMapper objectMapper = new ObjectMapper();
                        List<WebsiteDescription> websites = objectMapper.readValue(new File("/home/dev/Downloads/result/encode.json"),
                                objectMapper.getTypeFactory().constructCollectionType(List.class, WebsiteDescription.class));
                        for (WebsiteDescription websiteData : websites) {
                            if (!(websiteData.getUrl().toString().startsWith("http"))) {
                                websiteData.setUrl("https://" + website.getWebsite_url() + websiteData.getUrl());
                            }
                            List<WebsiteDescription> existingEntity = dataRepo.findAllByUrl(websiteData.getUrl());
                            if(!existingEntity.isEmpty()){
                                dataRepo.deleteAll(existingEntity);
                            }
                        }
                        dataRepo.saveAll(websites);

                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public String getlatesDate(String url) throws IOException {

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
        List<WebsiteDescription> li = dataRepo.findAll();
        List<WebsiteDescription> unsortList = new ArrayList<>();
        for (WebsiteDescription item : li){
            if (item.getUrl().contains(url)){
                unsortList.add(item);
            }
        }
        return unsortList;
    }
}
