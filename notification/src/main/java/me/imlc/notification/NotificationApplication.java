package me.imlc.notification;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class NotificationApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(NotificationApplication.class)
				.build()
				.run(args);
	}

}
