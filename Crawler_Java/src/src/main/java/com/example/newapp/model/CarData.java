package com.example.newapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarData {
    private String websiteName;
    private List<CarDescription> carDescriptions;
    private Long websiteId;
}
