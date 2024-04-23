package com.example.newapp.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCategoryRequest {
    String categoryName;
    String path;
}
