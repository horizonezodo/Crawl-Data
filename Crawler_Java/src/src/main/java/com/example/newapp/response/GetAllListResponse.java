package com.example.newapp.response;

import com.example.newapp.model.WebsiteDescription;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetAllListResponse {
    private Long id;
    private String url;
    private String title;
    private String detail;
    private String price;
    private String square;
    private String date;
    private String websiteName;
    private Long websiteId;
}
