package com.mif.demo.webupload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @description: 大文件断点续传demo
 * @author: mif
 * @date: 2018/2/9
 * @time: 11:06
 * @copyright: 拓道金服 Copyright (c) 2017
 */
@SpringBootApplication
public class WebUploadApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebUploadApplication.class, args);
	}
}
