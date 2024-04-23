package com.example.newapp.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SearchAllStockResponse {
    private Long id;
    private String url;
    private String stockCode;
    private String companyName;
    private String career;
    private String floor;
    private String date;
    private String price;
    private String websiteName;
    private Long websiteId;
}
