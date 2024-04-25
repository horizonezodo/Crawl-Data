package com.nextg.crawler.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class WebsiteData {
    private String websiteName;
    private List<WebsiteDescription> websiteDescription;
    private Long websiteId;

}
