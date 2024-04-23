package com.example.newapp.controll;

import com.example.newapp.model.CarDescription;
import com.example.newapp.model.StockDescription;
import com.example.newapp.model.Website;
import com.example.newapp.repo.StockDescriptionRepository;
import com.example.newapp.repo.WebsiteRepository;
import com.example.newapp.response.CrawlerResponse;
import com.example.newapp.response.ResponseError;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class StockController {
    @Autowired
    private WebsiteRepository webRepo;

    @Autowired
    private StockDescriptionRepository stockRepo;

    @Value("${run_python_path}")
    String run_python_path;

    @Value("${result_json}")
    String json_rs_file_path ;


    @GetMapping("/stockCrawl/{id}")
    public ResponseEntity<?> getAllDataFromCsv(@PathVariable("id") Long id) {
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
                    String latestDate = getlatesDate(website.getWebsite_url());
                    if (latestDate != null){
                        latestDate = latestDate.replace(" ", "T");
                    }else{
                        latestDate = null;
                    }
                    log.info(latestDate);
                    //String pythonScriptPath = "/home/dev/Downloads/crawler/crawler/spiders/runMethod.py";
                    //Process process = Runtime.getRuntime().exec("python /home/dev/Downloads/crawler/crawler/spiders/runMethod.py " + script_load_data_file_path + " " + latestDate);
                    Process process = Runtime.getRuntime().exec("python3 "+run_python_path+" " + website.getSpider_url() + " " + latestDate);
                    log.info("python3 "+run_python_path+" " + website.getSpider_url() + " " + latestDate);
                    //Process process = Runtime.getRuntime().exec("scrapy runspider " + website.getSpider_url() +" --logfile=/home/dev/filelog.txt");
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

                        String outputFile = json_rs_file_path+"/"+getOutputFile(website.getSpider_url())+".json";
                        log.info(outputFile);
                        List<StockDescription> carList = objectMapper.readValue(new File(outputFile),
                                objectMapper.getTypeFactory().constructCollectionType(List.class, StockDescription.class));
                        log.info(carList.toString());
                        if( latestDate != null){
                            log.info("thời gian nhận được " + latestDate);
                            List<StockDescription> allCarExists = new ArrayList<>();
                            for (StockDescription item: carList){
//                            if(!(item.getUrl().contains("http"))){
//                                item.setUrl(website.getWebsite_url()+item.getUrl());
//                            }
                                log.info(item.toString());
                                List<StockDescription> existingEntity = stockRepo.findAllByUrl(item.getUrl());
                                allCarExists.addAll(existingEntity);
                            }
                            stockRepo.deleteAll(allCarExists);
                        }
                        log.info("Lưu dâta vào db ");
                        stockRepo.saveAll(carList);

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

        List<StockDescription> unsortList = getAllWebsiteDescriptionWithUrl(url);
        if (!unsortList.isEmpty()){
            Collections.sort(unsortList, new Comparator<StockDescription>() {
                @Override
                public int compare(StockDescription o1, StockDescription o2) {
                    return o1.getDate().compareTo(o2.getDate());
                }
            });
            return unsortList.get(unsortList.size() -1).getDate();
        }
        else return null;
    }

    public List<StockDescription> getAllWebsiteDescriptionWithUrl(String url){
        List<StockDescription> li = stockRepo.findAllByContainingUrl(url);
//        List<CarDescription> unsortList = new ArrayList<>();
//        for (CarDescription item : li){
//            if (item.getUrl().contains(url)){
//                unsortList.add(item);
//            }
//        }
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

    @GetMapping("/auth/test-add-data")
    public ResponseEntity<?> addDataaForStock() throws IOException {
        log.info("Tiêns hành truyền dữ liệu từ file ra object ");
        ObjectMapper objectMapper = new ObjectMapper();
        List<StockDescription> stockList = objectMapper.readValue(new File("/opt/apache-tomcat-9.0.87/result/data.json"),
                objectMapper.getTypeFactory().constructCollectionType(List.class, StockDescription.class));
        stockRepo.saveAll(stockList);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
