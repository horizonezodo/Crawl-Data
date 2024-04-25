package com.nextg.crawler.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ChangePassResponse {
    private String email;
    private String new_pass;
}
