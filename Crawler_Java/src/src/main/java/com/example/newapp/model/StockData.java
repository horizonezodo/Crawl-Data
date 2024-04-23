package com.example.newapp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StockData {
    private String websiteName;
    private List<StockDescription> stockDescriptions;
    private Long websiteId;
}
