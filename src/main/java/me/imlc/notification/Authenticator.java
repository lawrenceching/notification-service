package me.imlc.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class Authenticator {

  private static Logger logger = LoggerFactory.getLogger(Authenticator.class);

  private String token;

  public Authenticator(@Autowired String token) {
    logger.info("Initialized Authenticator with token \"{}\"", token);
    this.token = token;
  }

  public boolean authenticate(String token) {
    return this.token.equals(token);
  }

}
