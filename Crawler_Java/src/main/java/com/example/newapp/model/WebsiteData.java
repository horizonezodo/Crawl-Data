package com.example.newapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class WebsiteData {
    private String websiteName;
    private List<WebsiteDescription> websiteDescription;
    private Long websiteId;

}
