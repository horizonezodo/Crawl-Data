package com.nextg.crawler.model;

import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@Entity(name = "website")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Website {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String website_name;
    private String website_url;
    private String spider_url;
    private String type;

}
