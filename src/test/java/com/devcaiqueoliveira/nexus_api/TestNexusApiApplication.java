package com.devcaiqueoliveira.nexus_api;

import org.springframework.boot.SpringApplication;

public class TestNexusApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(NexusApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
