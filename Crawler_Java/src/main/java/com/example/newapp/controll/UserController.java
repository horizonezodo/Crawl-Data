package com.example.newapp.controll;

import com.example.newapp.jwt.JwtUntils;
import com.example.newapp.model.*;
import com.example.newapp.repo.CarDescriptionRepository;
import com.example.newapp.repo.UserRepository;
import com.example.newapp.repo.WebsiteDescriptionRepository;
import com.example.newapp.repo.WebsiteRepository;
import com.example.newapp.request.UpdateUserInfoRequest;
import com.example.newapp.response.*;
import com.example.newapp.service.MailService;
import com.example.newapp.service.UserDetailsImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.swing.text.html.Option;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
@Slf4j
public class UserController {
    @Autowired
    UserRepository repo;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    WebsiteRepository webRepo;

    @Autowired
    WebsiteDescriptionRepository dataRepo;

    @Autowired
    CarDescriptionRepository carRepo;

    @GetMapping("/list-user")
    public ResponseEntity<?> getAllUser(@RequestParam(defaultValue = "0")int page, @RequestParam(defaultValue = "25")int size){
        PageRequest pageable = PageRequest.of(page,size);
        Page<User> user_list = repo.findAll(pageable);
        log.info("Get all user sucess");
        return new ResponseEntity<>(user_list, HttpStatus.OK);
    }

    @PutMapping("/user/{id}")
    public ResponseEntity<?> update_user_info(@PathVariable("id") Long id, @RequestBody UpdateUserInfoRequest request){
        Optional<User> otp = repo.findById(id);
        if (otp.isPresent()){
            User update_user = otp.get();
            update_user.setPassword(encoder.encode(request.getNew_pass()));
            repo.save(update_user);
            ChangePasswordResponse res = new ChangePasswordResponse(update_user.getEmail(), update_user.getPassword());
            log.info("Update user infomation success with user email " + update_user.getEmail());
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        log.error("Id not found " + id);
        ResponseError error = new ResponseError("Id not found");
        return new ResponseEntity<>(error,HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/user-info/{id}")
    public ResponseEntity<?> getUserInfo(@PathVariable("id")Long id){
        Optional<User> otp = repo.findById(id);
        if (otp.isPresent()){
            User user = otp.get();
            log.info("Get user infomation success with user email " + user.getEmail());
            return new ResponseEntity<>(user,HttpStatus.OK);
        }
        log.error("Id not found " + id);
        ResponseError error = new ResponseError("Id not found");
        return new ResponseEntity<>(error,HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping("/delete-user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id")Long id){
        Optional<User> otp = repo.findById(id);
        if (otp.isPresent()){
            User user = otp.get();
            repo.delete(user);
            log.info("Delete user infomation success with user email " + user.getEmail());
            return new ResponseEntity<>(user,HttpStatus.OK);
        }
        log.error("Id not found " + id);
        ResponseError error = new ResponseError("Id not found");
        return new ResponseEntity<>(error,HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/list-website-description")
    public Page<GetAllListResponse> getWebsiteData(@RequestParam(defaultValue = "0")int page, @RequestParam(defaultValue = "25")int size){
        PageRequest pageable = PageRequest.of(page,size);
        List<Website> listWebsite = webRepo.findAll();
        Page<WebsiteDescription> websiteDescriptions = dataRepo.findAll(pageable);
        Page<GetAllListResponse> websiteDescriptionDTOs = websiteDescriptions.map(websiteDescription -> {
            GetAllListResponse dto = new GetAllListResponse();
            dto.setId(websiteDescription.getId());
            dto.setDate(websiteDescription.getDate());
            dto.setDetail(websiteDescription.getDetail());
            dto.setPrice(websiteDescription.getPrice());
            dto.setTitle(websiteDescription.getTitle());
            dto.setSquare(websiteDescription.getSquare());
            dto.setUrl(websiteDescription.getUrl());
            for (WebsiteDescription item: websiteDescriptions){
                for (Website item2 : listWebsite){
                    if (item.getUrl().contains(item2.getWebsite_url())){
                        dto.setWebsiteName(item2.getWebsite_name());
                        dto.setWebsiteId(item2.getId());
                    }
                }
            }
            return dto;
        });
        return websiteDescriptionDTOs;
    }

    @GetMapping("/websites/count")
    public ResponseEntity<?> websiteDataCount(){
        getSizeResponse res = new getSizeResponse();
        res.setSize(dataRepo.count());
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @GetMapping("/website-description/{id}")
    public ResponseEntity<?> getWebsiteDescription(@PathVariable("id")Long id, @RequestParam(defaultValue = "0")int page, @RequestParam(defaultValue = "25")int size){
        Optional<Website> otp  = webRepo.getWebsiteById(id);
        if(otp.isPresent()){
            Website website = otp.get();
            Page<WebsiteDescription> dataList = dataRepo.findByUrlContaining(website.getWebsite_url(), PageRequest.of(page, size));
            log.info("get all website description with id: " + id);
            return new ResponseEntity<>(dataList, HttpStatus.OK);
        }
        log.error("Id not found");
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
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

    @GetMapping("/search")
    public Page<GetAllListResponse> userSearch(@RequestParam String keyword,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "200") int size,
                                               @RequestParam(required = false) String sortBy) {
        System.out.println(keyword);
        List<Website> listWebsite = webRepo.findAll();
        //Sort sortOrder = Sort.by(Sort.Direction.DESC, "date");
        Sort sortOrder = null;
        if (sortBy != null && !sortBy.isEmpty()) {
            sortOrder = Sort.by(sortBy.split(",")).descending();
        }
        PageRequest pageable = PageRequest.of(page, size, sortOrder);
        Page<WebsiteDescription> websiteDescriptions = dataRepo.search(keyword, pageable);

        // Convert Page to List and sort
        List<WebsiteDescription> sortedList = new ArrayList<>(websiteDescriptions.getContent());
        sortedList.sort((item1, item2) -> {
            if (sortBy != null && !sortBy.isEmpty() && !sortBy.contains("price") && sortBy.contains("date")) {

                return item2.getDate().compareTo(item1.getDate());
            } else {
                if (item1.getPrice() == null && item2.getPrice() == null) {
                    return 0;
                } else if (item1.getPrice() == null) {
                    return 1;
                } else if (item2.getPrice() == null) {
                    return -1;
                } else {
                    double price1 = getRealPrice(item1.getPrice(), getPriceValue(item1.getPrice()));
                    double price2 = getRealPrice(item2.getPrice(), getPriceValue(item2.getPrice()));
                    return Double.compare(price1, price2);
                }
            }
        });

        // Map sorted list to DTOs
        List<GetAllListResponse> websiteDescriptionDTOs = sortedList.stream().map(websiteDescription -> {
            GetAllListResponse dto = new GetAllListResponse();
            dto.setId(websiteDescription.getId());
            dto.setDate(websiteDescription.getDate());
            dto.setDetail(websiteDescription.getDetail());
            dto.setPrice(websiteDescription.getPrice());
            dto.setTitle(websiteDescription.getTitle());
            dto.setSquare(websiteDescription.getSquare());
            dto.setUrl(websiteDescription.getUrl());

            return dto;
        }).collect(Collectors.toList());
        for (GetAllListResponse item : websiteDescriptionDTOs) {
            for (Website item2 : listWebsite) {
                if (item.getUrl().contains(item2.getWebsite_url())) {
                    item.setWebsiteName(item2.getWebsite_name());
                    item.setWebsiteId(item2.getId());
                }
            }
        }
        return new PageImpl<>(websiteDescriptionDTOs, pageable, websiteDescriptions.getTotalElements());
    }




    @GetMapping("/search-with-id/{id}")
    public Page<WebsiteDescription> searchWebsiteDescription(@PathVariable("id") Long id,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "25") int size,
                                                      @RequestParam String keyword,
                                                      @RequestParam(required = false) String sortBy) {
        Optional<Website> otp = webRepo.getWebsiteById(id);
        if (otp.isPresent()) {
            Website website = otp.get();
//            Sort sortOrder = Sort.by(Sort.Direction.DESC, "date");
            Sort sortOrder = null;
            if (sortBy != null && !sortBy.isEmpty()) {
                sortOrder = Sort.by(sortBy.split(",")).descending();
            }
            PageRequest pageable = PageRequest.of(page, size, sortOrder);
            Page<WebsiteDescription> dataList = dataRepo.search2(keyword, website.getWebsite_url(), pageable);

            List<WebsiteDescription> sortedList = new ArrayList<>(dataList.getContent());
            sortedList.sort((item1, item2) -> {
                if (sortBy != null && !sortBy.isEmpty() && !sortBy.contains("price") && sortBy.contains("date")) {
                    // Sắp xếp theo trường date nếu sortBy không chứa trường price
                    return item2.getDate().compareTo(item1.getDate());
                } else {
                    if (item1.getPrice() == null && item2.getPrice() == null) {
                        System.out.println("both two values is null");
                        return 0;
                    } else if (item1.getPrice() == null) {
                        System.out.println(item1.toString() + "is null");
                        return 1;
                    } else if (item2.getPrice() == null) {
                        System.out.println(item2.toString() + " is null");
                        return -1;
                    } else {
                        double price1 = getRealPrice(item1.getPrice(), getPriceValue(item1.getPrice()));
                        double price2 = getRealPrice(item2.getPrice(), getPriceValue(item2.getPrice()));

                        System.out.println(item1.getTitle() + " gias " + getPriceValue(item1.getPrice()) + " " + item2.getTitle() + " gias" + getPriceValue(item2.getPrice()));
                        return Double.compare(price1, price2);
                    }
                }
            });

            log.info("get all website description with id: " + id);
            return new PageImpl<>(sortedList, pageable, dataList.getTotalElements());
        }
        log.error("Id not found");
        return Page.empty();
    }

    @GetMapping("/car-search")
    public Page<SearchAllListCarReponse> userCarSearch(@RequestParam String keyword,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "200") int size,
                                                    @RequestParam(required = false) String sortBy) {
        System.out.println(keyword);
        List<Website> listWebsite = webRepo.findAll();
//        Sort sortOrder = Sort.by(Sort.Direction.DESC, "date");
        Sort sortOrder = null;
        if (sortBy != null && !sortBy.isEmpty()) {
            sortOrder = Sort.by(sortBy.split(",")).descending();
        }
        PageRequest pageable = PageRequest.of(page, size, sortOrder);
        Page<CarDescription> carDescriptions = carRepo.search(keyword, pageable);

        // Convert Page to List and sort
        List<CarDescription> sortedList = new ArrayList<>(carDescriptions.getContent());
        sortedList.sort((item1, item2) -> {
            if (sortBy != null && !sortBy.isEmpty() && !sortBy.contains("price") && sortBy.contains("date")) {
                // Sắp xếp theo trường date nếu sortBy không chứa trường price
                return item2.getDate().compareTo(item1.getDate());
            } else {
                if (item1.getPrice() == null && item2.getPrice() == null) {
                    return 0;
                } else if (item1.getPrice() == null) {
                    return 1;
                } else if (item2.getPrice() == null) {
                    return -1;
                } else {
                    double price1 = getRealPrice(item1.getPrice(), getPriceValue(item1.getPrice()));
                    double price2 = getRealPrice(item2.getPrice(), getPriceValue(item2.getPrice()));
                    return Double.compare(price1, price2);
                }
            }
        });

        // Map sorted list to DTOs
        List<SearchAllListCarReponse> carDescriptionDTOs = sortedList.stream().map(carDescription -> {
            SearchAllListCarReponse dto = new SearchAllListCarReponse();
            dto.setId(carDescription.getId());
            dto.setDate(carDescription.getDate());
            dto.setDetail(carDescription.getDetail());
            dto.setPrice(carDescription.getPrice());
            dto.setTitle(carDescription.getTitle());
            dto.setGear(carDescription.getGear());
            dto.setType(carDescription.getType());
            dto.setUrl(carDescription.getUrl());

            return dto;
        }).collect(Collectors.toList());
        for (SearchAllListCarReponse item : carDescriptionDTOs) {
            for (Website item2 : listWebsite) {
                if (item.getUrl().contains(item2.getWebsite_url())) {
                    item.setWebsiteName(item2.getWebsite_name());
                    item.setWebsiteId(item2.getId());
                }
            }
        }
        return new PageImpl<>(carDescriptionDTOs, pageable, carDescriptions.getTotalElements());
    }




    @GetMapping("/search-car-with-id/{id}")
    public Page<CarDescription> searchCarDescription(@PathVariable("id") Long id,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "25") int size,
                                                             @RequestParam String keyword,
                                                             @RequestParam(required = false) String sortBy) {
        Optional<Website> otp = webRepo.getWebsiteById(id);
        if (otp.isPresent()) {
            Website website = otp.get();
            //        Sort sortOrder = Sort.by(Sort.Direction.DESC, "date");
            Sort sortOrder = null;
            if (sortBy != null && !sortBy.isEmpty()) {
                 sortOrder = Sort.by(sortBy.split(",")).descending();
            }
            PageRequest pageable = PageRequest.of(page, size, sortOrder);
            Page<CarDescription> dataList = carRepo.search2(keyword, website.getWebsite_url(), pageable);

            List<CarDescription> sortedList = new ArrayList<>(dataList.getContent());
            sortedList.sort((item1, item2) -> {
                if (sortBy != null && !sortBy.isEmpty() && !sortBy.contains("price") && sortBy.contains("date")) {
                    return item2.getDate().compareTo(item1.getDate());
                } else {
                    if (item1.getPrice() == null && item2.getPrice() == null) {
                        System.out.println("both two values is null");
                        return 0;
                    } else if (item1.getPrice() == null) {
                        System.out.println(item1.toString() + "is null");
                        return 1;
                    } else if (item2.getPrice() == null) {
                        System.out.println(item2.toString() + " is null");
                        return -1;
                    } else {
                        double price1 = getRealPrice(item1.getPrice(), getPriceValue(item1.getPrice()));
                        double price2 = getRealPrice(item2.getPrice(), getPriceValue(item2.getPrice()));

                        System.out.println(item1.getTitle() + " gias " + getPriceValue(item1.getPrice()) + " " + item2.getTitle() + " gias" + getPriceValue(item2.getPrice()));
                        return Double.compare(price1, price2);
                    }
                }
            });

            log.info("get all website description with id: " + id);
            return new PageImpl<>(sortedList, pageable, dataList.getTotalElements());
        }
        log.error("Id not found");
        return Page.empty();
    }

    private double getRealPrice(String priceType, double price){
        if(priceType.contains("Tỷ")){
            return price * 1000;
        }
        return price;
    }


    private double getPriceValue(String price) {
        if (price == null || price.trim().isEmpty() || price.trim().isBlank()) {
            return Double.MIN_VALUE;
        }
        price = price.replace(",", ".");
        price = price.replaceAll("[^\\d.]", "");
        if (price.trim().isEmpty()) {
            return 0.0;
        }
        System.out.println("gia tri chu " + price);
        double gia = Double.valueOf(price);
        System.out.println("Gia tri sau khi convert "+ gia);
        return gia;
    }

}
