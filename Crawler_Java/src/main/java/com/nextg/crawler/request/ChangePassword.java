package com.nextg.crawler.request;

import lombok.Data;

@Data
public class ChangePassword {
    String email;
    String new_pass;
    String old_pass;
}