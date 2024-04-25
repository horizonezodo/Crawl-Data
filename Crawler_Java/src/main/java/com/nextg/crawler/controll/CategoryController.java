package com.nextg.crawler.controll;

import com.nextg.crawler.model.Category;
import com.nextg.crawler.model.Website;
import com.nextg.crawler.repo.CategoryRepository;
import com.nextg.crawler.repo.WebsiteRepository;
import com.nextg.crawler.request.UpdateCategoryRequest;
import com.nextg.crawler.response.UpdateCartResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    CategoryRepository catRepo;

    @Autowired
    WebsiteRepository webRepo;

    @GetMapping("/all-category-page")
    public ResponseEntity<?> getallCategoryPage(@RequestParam(defaultValue = "0")int page, @RequestParam(defaultValue = "25")int size){
        PageRequest pageable = PageRequest.of(page,size);
        Page<Category> catList = catRepo.findAll(pageable);
        return new ResponseEntity<>(catList, HttpStatus.OK);
    }

    @GetMapping("/get-category/{id}")
    public ResponseEntity<?> getCategory(@PathVariable("id")Long id){
        Optional<Category> opt = catRepo.findById(id);
        if(opt.isPresent()){
            Category cat = opt.get();
            return new ResponseEntity<>(cat, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/all-category")
    public ResponseEntity<?> getAllCategory(){
        return new ResponseEntity<>(catRepo.findAll(), HttpStatus.OK);
    }

    @PostMapping("/add-category")
    public ResponseEntity<?> addCategory(@RequestBody Category category){
        return new ResponseEntity<>(catRepo.save(category), HttpStatus.CREATED);
    }

    @PutMapping("/update-category/{id}")
    public ResponseEntity<?> updateCategory(@RequestBody UpdateCategoryRequest req, @PathVariable("id") Long id){
        Optional<Category> opt = catRepo.findById(id);
        if(opt.isPresent()){
            Category tmpCat = opt.get();
            List<Website> li = webRepo.findAllByType(tmpCat.getCategoryName());

            if(req.getPath() != null && !req.getPath().isEmpty() &&!req.getPath().isBlank()){
                tmpCat.setPath(req.getPath());
            }
            if(req.getCategoryName() != null && !req.getCategoryName().isEmpty() && !req.getCategoryName().isBlank()){
                tmpCat.setCategoryName(req.getCategoryName());
                for (Website item: li){
                    item.setType(req.getCategoryName());
                }
            }
            webRepo.saveAll(li);
            catRepo.save(tmpCat);
            UpdateCartResponse response = new UpdateCartResponse();
            response.setCategoryName(tmpCat.getCategoryName());
            response.setPath(tmpCat.getPath());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping("/delete-category/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable("id") Long id){
        Optional<Category> opt = catRepo.findById(id);
        if(opt.isPresent()){
            Category tmpCat = opt.get();
            catRepo.delete(tmpCat);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
