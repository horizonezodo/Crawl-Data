package com.example.newapp.controll;
import com.example.newapp.model.User;
import com.example.newapp.model.Website;
import com.example.newapp.model.WebsiteData;
import com.example.newapp.model.WebsiteDescription;
import com.example.newapp.repo.UserRepository;
import com.example.newapp.repo.WebsiteDescriptionRepository;
import com.example.newapp.repo.WebsiteRepository;
import com.example.newapp.request.RegisterRequest;
import com.example.newapp.response.*;
import com.example.newapp.service.MailService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    Path root = Paths.get("/home/dev/Downloads/uploads");

    @Value("${result_json}")
    String json_rs_file_path ;
    
    @Autowired
    MailService service;
    
    @Autowired
    UserRepository repo;
    
    @Autowired
    PasswordEncoder encoder;
    
    @Autowired
    WebsiteDescriptionRepository dataRepo;

    @PersistenceContext
    EntityManager entityManager;
    
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
        Optional<Website> otp = webRepo.getWebsiteById(id);
        if (otp.isPresent()){
            Website newWebsite = otp.get();
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
        try {
            if (!(Files.exists(root))){
                Files.createDirectories(root);
            }
            Path targetPath = this.root.resolve(file.getOriginalFilename());
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

                        ObjectMapper objectMapper = new ObjectMapper();
                        List<WebsiteDescription> websites = objectMapper.readValue(new File(json_rs_file_path),
                                objectMapper.getTypeFactory().constructCollectionType(List.class, WebsiteDescription.class));
                        List<WebsiteDescription> allWebsiteExists = new ArrayList<>();
                        for (WebsiteDescription item: websites){
                            if(!(item.getUrl().toString().startsWith("http"))){
                                item.setUrl(website.getWebsite_url()+item.getUrl());
                            }
                            List<WebsiteDescription> existingEntity = dataRepo.findAllByUrl(item.getUrl());
                            allWebsiteExists.addAll(existingEntity);
                        }
                        dataRepo.deleteAll(allWebsiteExists);
                        dataRepo.saveAll(websites);

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

    @GetMapping("/all")
    public ResponseEntity<?> getAllData() throws IOException, InterruptedException {
        List<Website> li = webRepo.findAll();
        List<WebsiteData> dataList = new ArrayList<>();
        for (Website website: li){
            WebsiteData data = new WebsiteData();
            data.setWebsiteId(website.getId());
            data.setWebsiteName(website.getWebsite_name());
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
                    List<String> urls = new ArrayList<>();
                    for (WebsiteDescription websiteData: websites){
                    	if(!(websiteData.getUrl().toString().startsWith("http"))){
                            websiteData.setUrl("https://"+website.getWebsite_url()+websiteData.getUrl());
                        }
                        urls.add(websiteData.getUrl());
                        deleteByUrls(urls);
//                        List<WebsiteDescription> existingEntity = dataRepo.findAllByUrl(websiteData.getUrl());
//                        if(!existingEntity.isEmpty()){
//                            dataRepo.deleteAll(existingEntity);
//                        }
                    }
                    dataRepo.saveAll(websites);
                    List<WebsiteDescription> result = getAllWebsiteDescriptionWithUrl(website.getWebsite_url());
                    data.setWebsiteDescription(result);
                }
                dataList.add(data);

            }
        }
        if (dataList.size() > 0) {
            return new ResponseEntity<>(dataList, HttpStatus.OK);
        } else {
            ResponseError error = new ResponseError();
            error.setErrorMessage("Sai định dạng file");
            log.error("File Type Error");
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
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

    @Transactional
    public void deleteByUrls(List<String> urls) {
        if (urls != null && !urls.isEmpty()) {
            String deleteQuery = "DELETE FROM WebsiteDescription wd WHERE wd.url IN :urls";
            entityManager.createQuery(deleteQuery)
                    .setParameter("urls", urls)
                    .executeUpdate();
        }
    }
}