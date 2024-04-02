package com.example.newapp.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchAllListCarReponse {
    private Long id;
    private String url;
    private String title;
    private String detail;
    private String price;
    private String gear;
    private String type;
    private String date;
    private String websiteName;
    private Long websiteId;
}
