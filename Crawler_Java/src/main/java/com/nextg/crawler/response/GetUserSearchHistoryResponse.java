package com.nextg.crawler.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetUserSearchHistoryResponse {
    String email;
    String categoryName;
    String keyword;
    String sortBy;
}
