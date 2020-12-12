package me.imlc.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NotificationApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationApplication.class, args);

		GreetingWebClient gwc = new GreetingWebClient();
		System.out.println(gwc.getResult());
	}

}
