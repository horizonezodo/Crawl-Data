package com.nextg.crawler.controll;

import com.nextg.crawler.configuration.AppConfig;
import com.nextg.crawler.model.CarDescription;
import com.nextg.crawler.model.Website;
import com.nextg.crawler.repo.CarDescriptionRepository;
import com.nextg.crawler.repo.WebsiteRepository;
import com.nextg.crawler.response.CrawlerResponse;
import com.nextg.crawler.response.ResponseError;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.*;

@RestController
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class CarController {
    @Autowired
    private WebsiteRepository webRepo;

    @Autowired
    private CarDescriptionRepository dataRepo;

    @Autowired
    com.nextg.crawler.service.deleteDuplicateRecordService deleteDuplicateRecordService;

    @GetMapping("/crawlCarData/{id}")
    public ResponseEntity<?> CarCrawlData(@PathVariable("id") Long id) {
        try {
            Optional<Website> otp = webRepo.getWebsiteById(id);
            if (otp.isEmpty()) {
                ResponseError error = new ResponseError();
                error.setErrorMessage("Không tìm thấy id này");
                log.error("ID not found: ");
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            } else {
                Website website = otp.get();
                String script_load_data_file_path = website.getSpider_url();
                log.info(script_load_data_file_path);
                if (script_load_data_file_path.endsWith(".py")){
                    log.info("File is python file");
                    String latestDate = getlatesDate(website.getWebsite_url());
                    if (latestDate != null){
                        latestDate = latestDate.replace(" ", "T");
                    }else{
                        latestDate = null;
                    }
                    log.info(latestDate);
                    String[] command = {
                            "/bin/bash",
                            "-c",
                            "cd "+ AppConfig.scrapyWorkDir +" && source "+AppConfig.pythonEnviromentFolder+"/bin/activate && scrapy runspider " + website.getSpider_url() + " -a pass_date_str='" + latestDate + "' --logfile="+AppConfig.scrapyWorkDir+"/file_log.txt"};
                    Process process = Runtime.getRuntime().exec(command);
                    log.info("crawl data");
                    log.info("scrapy runspider " + website.getSpider_url());
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                        log.info(line);
                    }
                    while ((line = errorReader.readLine()) != null) {
                        System.out.println(line);
                        log.info(line);
                    }

                    int exitCode = process.waitFor();
                    log.info(String.valueOf(exitCode));

                    if (exitCode == 0) {
                        InputStream inputStream = process.getInputStream();
                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);


                        while ((line = bufferedReader.readLine()) != null) {
                            System.out.println(line);
                            log.info(line);
                        }
                        log.info("Tiêns hành truyền dữ liệu từ file ra object ");
                        ObjectMapper objectMapper = new ObjectMapper();

                        String outputFile = AppConfig.resultFolderWorkDir + getOutputFile(website.getSpider_url())+".json";
                        log.info(outputFile);
                        List<CarDescription> carList = objectMapper.readValue(new File(outputFile),
                                objectMapper.getTypeFactory().constructCollectionType(List.class, CarDescription.class));
                        log.info(carList.toString());
                        if( latestDate != null){
                            log.info("thời gian nhận được " + latestDate);
                            deleteDuplicateRecordService.deleteCarDuplicateRecords();
                            List<CarDescription> deleteEmpyPrice = dataRepo.findAllByContainingPrice("thoa thuan");
                            log.info("delete Empty price and price values like 'thỏa thuận' from cardescription");
                            dataRepo.deleteAll(deleteEmpyPrice);
                        }
                        log.info("Lưu dâta vào db ");
                        dataRepo.saveAll(carList);
                        deleteDuplicateRecordService.deleteCarDuplicateRecords();
                        List<CarDescription> deleteEmpyPrice = dataRepo.findAllByContainingPrice("thoa thuan");
                        log.info("delete Empty price and price values like 'thỏa thuận' from cardescription");
                        dataRepo.deleteAll(deleteEmpyPrice);

                        CrawlerResponse result = new CrawlerResponse();
                        result.setCrawler_result(true);
                        return new ResponseEntity<>(result, HttpStatus.OK);
                    }
                    ResponseError error = new ResponseError();
                    error.setErrorMessage("chay file spider gap van de ");
                    log.error("chay file spider gap van de" );
                    return new ResponseEntity<>(error,HttpStatus.BAD_REQUEST);
                }
                ResponseError error = new ResponseError();
                error.setErrorMessage("Sai định dạng file");
                log.error("File Type Error" );
                return new ResponseEntity<>(error,HttpStatus.BAD_REQUEST);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        log.error("Run python script code in cmd Fail : ");
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    public String getlatesDate(String url) throws IOException {

        List<CarDescription> unsortList = getAllWebsiteDescriptionWithUrl(url);
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

    public List<CarDescription> getAllWebsiteDescriptionWithUrl(String url){
        List<CarDescription> li = dataRepo.findAllByContainingUrl(url);
        return li;
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

}
