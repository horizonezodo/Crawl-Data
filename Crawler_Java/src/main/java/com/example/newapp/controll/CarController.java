package com.example.newapp.controll;

import com.example.newapp.model.CarDescription;
import com.example.newapp.model.Website;
import com.example.newapp.model.WebsiteDescription;
import com.example.newapp.repo.CarDescriptionRepository;
import com.example.newapp.repo.WebsiteRepository;
import com.example.newapp.response.CrawlerResponse;
import com.example.newapp.response.ResponseError;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class CarController {
    @Autowired
    private WebsiteRepository webRepo;

    @Autowired
    private CarDescriptionRepository dataRepo;

    Path root = Paths.get("/home/dev/Downloads/uploads");

    @Value("${result_json}")
    String json_rs_file_path ;

    @GetMapping("/crawlCarData/{id}")
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
                    String pythonScriptPath = "/home/dev/Downloads/crawler/crawler/spiders/runMethod.py";
                    Process process = Runtime.getRuntime().exec("python /home/dev/Downloads/crawler/crawler/spiders/runMethod.py " + script_load_data_file_path + " " + latestDate);
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
                        List<CarDescription> carList = objectMapper.readValue(new File(json_rs_file_path),
                                objectMapper.getTypeFactory().constructCollectionType(List.class, CarDescription.class));
                        log.info(carList.toString());
                        List<CarDescription> allCarExists = new ArrayList<>();
                        for (CarDescription item: carList){
                            if(!(item.getUrl().contains("http"))){
                                item.setUrl(website.getWebsite_url()+item.getUrl());
                            }
                            log.info(item.toString());
                            List<CarDescription> existingEntity = dataRepo.findAllByUrl(item.getUrl());
                            allCarExists.addAll(existingEntity);
                        }
                        dataRepo.deleteAll(allCarExists);
                        log.info("Lưu dâta vào db ");
                        dataRepo.saveAll(carList);

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
        List<CarDescription> li = dataRepo.findAll();
        List<CarDescription> unsortList = new ArrayList<>();
        for (CarDescription item : li){
            if (item.getUrl().contains(url)){
                unsortList.add(item);
            }
        }
        return unsortList;
    }

    @GetMapping("/auth/test")
    public ResponseEntity<?> Test() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<CarDescription> carList = objectMapper.readValue(new File("/home/dev/Desktop/otoxehoi.json"),
                objectMapper.getTypeFactory().constructCollectionType(List.class, CarDescription.class));
        log.info(carList.toString());
        System.out.println(carList);
        return new ResponseEntity<>(carList,HttpStatus.OK);
    }

}
