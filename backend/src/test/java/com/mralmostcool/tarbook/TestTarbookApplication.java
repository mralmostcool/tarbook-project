package com.mralmostcool.tarbook;

import org.springframework.boot.SpringApplication;

public class TestTarbookApplication {

	public static void main(String[] args) {
		SpringApplication.from(TarbookApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
