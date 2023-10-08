package com.base;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.base.controller.MainCOntroller;

@SpringBootApplication
public class WebApp1Application {

	public static void main(String[] args) throws Exception {
	ConfigurableApplicationContext run = SpringApplication.run(WebApp1Application.class, args);
		
	MainCOntroller bean = run.getBean(MainCOntroller.class);
	
	//bean.uploadFIle();
	
	
	}

}
