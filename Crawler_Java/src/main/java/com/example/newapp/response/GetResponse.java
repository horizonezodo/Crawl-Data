package com.example.newapp.response;

import lombok.Data;

@Data
public class GetResponse {
    String name;
    String url;
    String spider_url;
	String type;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getSpider_url() {
		return spider_url;
	}
	public void setSpider_url(String spider_url) {
		this.spider_url = spider_url;
	}
}
