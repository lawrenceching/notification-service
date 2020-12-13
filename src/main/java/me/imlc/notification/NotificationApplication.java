package me.imlc.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class NotificationApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(NotificationApplication.class)
				.build()
				.run(args);
	}

}
