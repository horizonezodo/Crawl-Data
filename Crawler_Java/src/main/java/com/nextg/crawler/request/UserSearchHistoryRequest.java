package com.nextg.crawler.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserSearchHistoryRequest {
    String email;
    Long categoryId;
    String keyword;
    String sortBy;
}
