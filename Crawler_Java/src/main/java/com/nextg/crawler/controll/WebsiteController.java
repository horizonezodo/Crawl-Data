package com.nextg.crawler.controll;

import com.nextg.crawler.configuration.AppConfig;
import com.nextg.crawler.model.*;
import com.nextg.crawler.request.RegisterRequest;

import com.nextg.crawler.service.MailService;
import com.nextg.crawler.service.deleteDuplicateRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextg.crawler.repo.*;
import com.nextg.crawler.response.CrawlerResponse;
import com.nextg.crawler.response.GetNameResponse;
import com.nextg.crawler.response.GetResponse;
import com.nextg.crawler.response.ResponseError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.mail.MessagingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

@RestController
@CrossOrigin(origins = "*",maxAge = 3600)
@Slf4j
public class WebsiteController {
    @Autowired
    WebsiteRepository webRepo;

    @Autowired
    MailService service;
    
    @Autowired
    UserRepository repo;
    
    @Autowired
    PasswordEncoder encoder;
    
    @Autowired
    WebsiteDescriptionRepository dataRepo;

    @Autowired
    CarDescriptionRepository carRepo;

    @Autowired
    StockDescriptionRepository stockRepo;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    SearchHistoryRepository searchRepo;

    @Autowired
    com.nextg.crawler.service.deleteDuplicateRecordService deleteDuplicateRecordService;
    
    @PostMapping("/auth/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) throws MessagingException {
        if(repo.existsByEmail(request.getEmail())){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }else{
            User user = new User();
            Random random = new Random();
            int minValue = 1000;
            int maxValue = 10000;
            int randomNumber = random.nextInt(maxValue - minValue + 1) + minValue;
            user.setUser_role(request.getUser_role());
            user.setEmail(request.getEmail());
            user.setUsername(request.getUsername());
            user.setPassword(encoder.encode(String.valueOf(randomNumber)));
            User saved_user = repo.save(user);
            sendEmailChangePass(saved_user.getEmail(),String.valueOf(randomNumber));

            return new ResponseEntity<>(saved_user, HttpStatus.CREATED);
        }
    }

    private void sendEmailChangePass(String email,String password) throws MessagingException {
        service.SendMail(email,password);
    }
    

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllWebsite(@RequestParam(defaultValue = "0")int page, @RequestParam(defaultValue = "25")int size){
        PageRequest pageable = PageRequest.of(page,size);
        Page<Website> webList = webRepo.findAll(pageable);
        log.info("Get all list website Success : " );

        return new ResponseEntity<>(webList,HttpStatus.OK);
    }

    @PostMapping(value="/add-url", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createNewWebsite(@RequestParam("name") String name, @RequestParam("url")String url, @RequestParam("file")MultipartFile file, @RequestParam("type")String type){
        Path root = Paths.get(AppConfig.uploadsFolderWorkDir);
        Website newWebsite = new Website();
        newWebsite.setWebsite_url(url);
        newWebsite.setWebsite_name(name);
        if (!(getFileName(file).equals(""))) {
            ResponseEntity<?> uploadResponse = UploadFile(file,1);
            if (uploadResponse.getStatusCode() == HttpStatus.CONFLICT) {
                log.error("File is conflict name with other file  : " );
                return uploadResponse;
            }
            newWebsite.setSpider_url(root + "/" + getFileName(file));
        }
        newWebsite.setType(type);
        Website saveWebsite = webRepo.save(newWebsite);
        log.info("create new website Success : " );
        return new ResponseEntity<>(saveWebsite, HttpStatus.CREATED);
    }

    @GetMapping("/getName/{id}")
    public ResponseEntity<?> getNameWebsite(@PathVariable("id") Long id){
        Optional<Website> otp = webRepo.getWebsiteById(id);
        if (otp.isPresent()){
            Website getWebsite = otp.get();
            GetNameResponse name = new GetNameResponse();
            name.setName(getWebsite.getWebsite_name());
            log.info("Get name website Success : " );
            return new ResponseEntity<>(name, HttpStatus.OK);
        }
        ResponseError error = new ResponseError();
        error.setErrorMessage("Id này không tồn tại");
        log.error("ID not found: " );
        return new ResponseEntity<>(error,HttpStatus.BAD_REQUEST);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateWebsite(@PathVariable("id") Long id, @RequestParam("name")String name,@RequestParam("url")String url,@RequestParam("file")MultipartFile file, @RequestParam("type")String type){
        Path root = Paths.get(AppConfig.uploadsFolderWorkDir);
        Optional<Website> otp = webRepo.getWebsiteById(id);
        if (otp.isPresent()){
            Website newWebsite = otp.get();
            Path path = Paths.get(newWebsite.getSpider_url());
            try {
                Files.delete(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            newWebsite.setWebsite_name(name);
            newWebsite.setWebsite_url(url);
            if (!(getFileName(file).equals(""))){
                ResponseEntity<?> uploadResponse = UploadFile(file,0);
                if (uploadResponse.getStatusCode() == HttpStatus.CONFLICT) {
                    log.error("File is conflict name with other file  : " );
                    return uploadResponse;
                }
                newWebsite.setSpider_url(root + "/" + getFileName(file));
            }
            newWebsite.setType(type);
            Website savedWebsite = webRepo.save(newWebsite);
            log.info("Update website Success : " );
            return new ResponseEntity<>(savedWebsite, HttpStatus.OK);
        }
        ResponseError error = new ResponseError();
        log.error("Update website info Fail : " );
        return new ResponseEntity<>(error,HttpStatus.BAD_REQUEST);
    }

    @PutMapping("/update-no-file/{id}")
    public ResponseEntity<?> updateWebsiteNoFile(@PathVariable("id") Long id, @RequestParam("name")String name,@RequestParam("url")String url,@RequestParam("type")String type){
        Optional<Website> otp = webRepo.getWebsiteById(id);
        if (otp.isPresent()){
            Website newWebsite = otp.get();
            newWebsite.setWebsite_name(name);
            newWebsite.setWebsite_url(url);
            newWebsite.setType(type);
            Website savedWebsite = webRepo.save(newWebsite);
            log.info("update website with no file Success : " );
            return new ResponseEntity<>(savedWebsite, HttpStatus.OK);
        }
        ResponseError error = new ResponseError();
        error.setErrorMessage("update thất bại");
        log.error("Update website infor with no file Fail : " );
        return new ResponseEntity<>(error,HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteWebsite(@PathVariable("id") Long id){
        Optional<Website> otp = webRepo.getWebsiteById(id);
        if (otp.isPresent()){
            Website newWebsite = otp.get();
            Path path = Paths.get(newWebsite.getSpider_url());
            try {
                Files.delete(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            webRepo.delete(newWebsite);
            log.info("Delete website Success : " );
            return new ResponseEntity<>(HttpStatus.OK);
        }
        ResponseError error = new ResponseError();
        error.setErrorMessage("Xóa thất bại");
        log.error("Delete website Fail : " );
        return new ResponseEntity<>(error,HttpStatus.BAD_REQUEST);
    }


    @GetMapping("/get/{id}")
    public ResponseEntity<?> getConfig(@PathVariable("id") Long id){
        Optional<Website> otp = webRepo.getWebsiteById(id);
        if (otp.isPresent()){
            Website newWebsite = otp.get();
            GetResponse res = new GetResponse();
            res.setName(newWebsite.getWebsite_name());
            res.setUrl(newWebsite.getWebsite_url());
            String str = newWebsite.getSpider_url();
            String fileName = str.substring(str.lastIndexOf("/") + 1);
            res.setSpider_url(fileName);
            res.setType(newWebsite.getType());
            log.info("Open config website Success : " );
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        ResponseError error = new ResponseError();
        error.setErrorMessage("Không lấy được config");
        log.error("Open config website Fail : " );
        return new ResponseEntity<>(error ,HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<?> UploadFile(MultipartFile file,int typeAction) {
        Path root = Paths.get(AppConfig.uploadsFolderWorkDir);
        try {
            if (!(Files.exists(root))){
                Files.createDirectories(root);
            }
            Path targetPath = root.resolve(file.getOriginalFilename());
            if (typeAction == 1){
                if (Files.exists(targetPath)) {
                    ResponseError error = new ResponseError();
                    error.setErrorMessage("A file of that name already exists.");
                    log.error("Upload file has conflict name with other file : " );
                    return new ResponseEntity<>(error, HttpStatus.CONFLICT);
                }
                try (InputStream in = file.getInputStream()) {
                    log.info("Upload file Success : " );
                    Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }else{
                try (InputStream in = file.getInputStream()) {
                    log.info("Upload file Success : " );
                    Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("Uploaded the file Success : " );
        return ResponseEntity.ok("File uploaded successfully.");
    }

    private String getFileName(MultipartFile file){
        String fileName = file.getOriginalFilename();
        return fileName;
    }

    @GetMapping("/getAllData/{id}")
    public ResponseEntity<?> CrawlWebsiteData(@PathVariable("id") Long id) {
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
                    String latestDate = getWebsitelatesDate(website.getWebsite_url());
                    if (latestDate != null){
                        latestDate = latestDate.replace(" ", "T");
                    }else{
                        latestDate = null;
                    }
                    log.info(latestDate);

                    String[] command = {
                            "/bin/bash",
                            "-c",
                            "cd "+AppConfig.scrapyWorkDir+" && source "+AppConfig.pythonEnviromentFolder+"/bin/activate && scrapy runspider " + website.getSpider_url() + " -a pass_date_str='" + latestDate + "' --logfile="+AppConfig.scrapyWorkDir+"/"+getOutputFile(website.getSpider_url())+".txt"};
                    Process process = Runtime.getRuntime().exec(command);
                    log.info("Start crawl");
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

                        String outputFile = AppConfig.resultFolderWorkDir+ (getOutputFile(website.getSpider_url()))+".json";

                        List<WebsiteDescription> websites = objectMapper.readValue(new File(outputFile),
                                objectMapper.getTypeFactory().constructCollectionType(List.class, WebsiteDescription.class));
                        if(latestDate != null){
                            log.info("thời gian nhận được " + latestDate);
                            List<WebsiteDescription> deleteEmptyPriceValue = dataRepo.findAllByContainingPrice("thoa thuan");
                            dataRepo.deleteAll(deleteEmptyPriceValue);
                            deleteDuplicateRecordService.deleteWebsiteDuplicateRecords();
                        }
                        log.info("Lưu dâta vào db ");
                        dataRepo.saveAll(websites);
                        List<WebsiteDescription> deleteEmptyPriceValue = dataRepo.findAllByContainingPrice("thoa thuan");
                        dataRepo.deleteAll(deleteEmptyPriceValue);
                        deleteDuplicateRecordService.deleteWebsiteDuplicateRecords();

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


    @GetMapping("/all2")
    public ResponseEntity<?> CrawlAllData2() throws IOException, InterruptedException {
        log.info("Run Job");
        try {
            List<Website> li = webRepo.findAll();
            ExecutorService executor = Executors.newFixedThreadPool(5); // Tạo hồ bơi luồng với 5 luồng
            log.info("Khởi tạo luồng ");
            for (Website website : li) {
                executor.submit(() -> { // Gửi một tác vụ mới cho hồ bơi luồng
                    log.info("Gửi tác vụ tới luồng");
                    String script_load_data_file_path = website.getSpider_url();
                    log.info(script_load_data_file_path);
                    if (script_load_data_file_path.endsWith(".py")) {
                        try {
                            String latestDate = getLatestDate(website.getType(), website.getWebsite_url());
                            latestDate = latestDate.replace(" ", "T");
                            log.info(latestDate);
                            String fileLogName = getOutputFile(website.getSpider_url());
                            String[] command = {
                                    "/bin/bash",
                                    "-c",
                                    "cd "+AppConfig.scrapyWorkDir+" && source "+AppConfig.pythonEnviromentFolder+"/venv/bin/activate && scrapy runspider " + website.getSpider_url() + " -a pass_date_str='" + latestDate + "' --logfile="+AppConfig.scrapyWorkDir+"/"+fileLogName+".txt"};
                            log.info("Starting crawler with website " + website.getSpider_url());
                            ProcessBuilder processBuilder = new ProcessBuilder(command);
                            processBuilder.redirectErrorStream(true);
                            Process process = processBuilder.start();
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
                                String outputFile = AppConfig.resultFolderWorkDir + getOutputFile(website.getSpider_url()) + ".json";
                                log.info(outputFile);
                                transferData(website.getType(), outputFile);
                            }
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            executor.shutdown();
            log.info("Thoát luồng hiện tại");
            try {
                if (!executor.awaitTermination(180, TimeUnit.MINUTES)) { // Chờ tất cả các tác vụ hoàn thành trong vòng 180 phút
                    log.info("Hết thời gian chờ đợi tự động đóng luồng");
                    executor.shutdownNow(); // Hủy tất cả các tác vụ đang chờ và đang chạy nếu chúng không hoàn thành trong thời gian đã định
                }
            } catch (InterruptedException e) {
                log.info("Luồng gặp lỗi tự động thoát luồng lỗi gặp phải : " + e.getMessage());
                executor.shutdownNow();
            }
            return new ResponseEntity<>(HttpStatus.OK);
        } catch(Exception e){
            e.printStackTrace();
        }
        log.error("Run python script code in cmd Fail : ");
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/test")
    public ResponseEntity<?> CrawlAllData() throws IOException, InterruptedException {
        log.info("Run Job");
        try {
            List<Website> li = webRepo.findAll();
            ExecutorService executor = Executors.newFixedThreadPool(5); // Tạo hồ bơi luồng với 5 luồng
            log.info("Khởi tạo luồng ");
            for (Website website : li) {
                executor.submit(() -> { // Gửi một tác vụ mới cho hồ bơi luồng
                    log.info(website.getSpider_url());
                    String script_load_data_file_path = website.getSpider_url();
                    log.info(script_load_data_file_path);
                });
            }

            executor.shutdownNow();
        }catch(Exception e){
            e.printStackTrace();
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<?> crawlData() throws IOException, InterruptedException {
        List<Website> li = webRepo.findAll();
        int bathSize = 3;
        for (int i = 0; i < li.size(); i+= bathSize) {
            List<Website> batch = new ArrayList<>();
            int end = Math.min(i + bathSize, li.size());
            for(int j = i; j < end; j++) {
                batch.add(li.get(j));
            }
            ProcessSpider(batch);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }


    public void ProcessSpider(List<Website> listWebsite) throws IOException, InterruptedException {
        try {
            List<Website> li =listWebsite;
            String commandText = "";
            for (Website website : li) {
                String script_load_data_file_path = website.getSpider_url();
                log.info(script_load_data_file_path);
                if (script_load_data_file_path.endsWith(".py")) {
                    String latestDate = getLatestDate(website.getType(), website.getWebsite_url());
                    latestDate = latestDate.replace(" ", "T");
                    log.info(latestDate);
                    String fileLogName = getOutputFile(website.getSpider_url());
                    commandText += "scrapy runspider " + website.getSpider_url() +" -a pass_date_str='" + latestDate + "' --logfile="+AppConfig.scrapyWorkDir+"/"+fileLogName+".txt &";
                    }
                }
            String[] command = {
                    "/bin/bash",
                    "-c",
                    "cd "+AppConfig.scrapyWorkDir+" && source "+AppConfig.pythonEnviromentFolder+"/venv/bin/activate && " + commandText };
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
                for(Website item: li){
                    String outputFile = AppConfig.resultFolderWorkDir +getOutputFile(item.getSpider_url())+".json";
                    if(item.getType().equalsIgnoreCase("bds")){
                        ObjectMapper objectMapper = new ObjectMapper();
                        log.info("Transfer Data to website descriptions");
                        List<WebsiteDescription> websites = objectMapper.readValue(new File(outputFile),
                                objectMapper.getTypeFactory().constructCollectionType(List.class, WebsiteDescription.class));
                        dataRepo.saveAll(websites);
                        log.info("Save to website descriptions");
                    }
                    else if(item.getType().equalsIgnoreCase("auto")){
                        ObjectMapper objectMapper = new ObjectMapper();
                        log.info("Transfer Data to car descriptions");
                        List<CarDescription> cars = objectMapper.readValue(new File(outputFile),
                                objectMapper.getTypeFactory().constructCollectionType(List.class, CarDescription.class));
                        carRepo.saveAll(cars);
                        log.info("Save to car descriptions");
                    }
                    else if(item.getType().equalsIgnoreCase("stock")){
                        ObjectMapper objectMapper = new ObjectMapper();
                        log.info("Transfer Data to stock descriptions");
                        List<StockDescription> stocks = objectMapper.readValue(new File(outputFile),
                                objectMapper.getTypeFactory().constructCollectionType(List.class, StockDescription.class));
                        stockRepo.saveAll(stocks);
                        log.info("save stock descriptions");
                    }
                    log.info(outputFile);
                }
            }
            log.info("Chạy xong tất cả các file spider");

        }catch(Exception e){
            e.printStackTrace();
            log.error("Run python script code in cmd Fail : " + e.getMessage());
        }

    }

    public void transferData(String type, String outputFile){
        try{
            if(type.equalsIgnoreCase("bds")){
                ObjectMapper objectMapper = new ObjectMapper();
                log.info("Transfer Data to website descriptions");
                List<WebsiteDescription> websites = objectMapper.readValue(new File(outputFile),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, WebsiteDescription.class));
                dataRepo.saveAll(websites);
                log.info("Save to website descriptions");
            }
            else if(type.equalsIgnoreCase("auto")){
                ObjectMapper objectMapper = new ObjectMapper();
                log.info("Transfer Data to car descriptions");
                List<CarDescription> cars = objectMapper.readValue(new File(outputFile),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, CarDescription.class));
                carRepo.saveAll(cars);
                log.info("Save to car descriptions");
            }
            else if(type.equalsIgnoreCase("stock")){
                ObjectMapper objectMapper = new ObjectMapper();
                log.info("Transfer Data to stock descriptions");
                List<StockDescription> stocks = objectMapper.readValue(new File(outputFile),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, StockDescription.class));
                stockRepo.saveAll(stocks);
                log.info("save stock descriptions");
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String getLatestDate(String type, String url) throws IOException {
        if(type.equalsIgnoreCase("bds")){
            return getWebsitelatesDate(url);
        }else if (type.equalsIgnoreCase("auto")){
            return getCarlatesDate(url);
        }
        return null;
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

    public List<CarDescription> getAllCarDescriptionWithUrl(String url){
        List<CarDescription> li = carRepo.findAllByContainingUrl(url);
        return li;
    }

    public List<WebsiteDescription> getAllWebsiteDescriptionWithUrl(String url){
        List<WebsiteDescription> li = dataRepo.findAllWebsiteContainingUrl(url);
        return li;
    }

    public String getOutputFile(String spiderUrl) {
        File file = new File(spiderUrl);
        String filename = file.getName();

        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex != -1) {
            filename = filename.substring(0, dotIndex);
        }
        log.info(filename);
        return filename;
    }

//    public boolean remaneFileLog(String oldUrl){
//        String newUrl = AppConfig.
//    }
}