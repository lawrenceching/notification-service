package me.imlc.notification;

import org.springframework.beans.factory.annotation.Autowired;

public class Authenticator {

  private String token;

  public Authenticator(@Autowired String token) {
    this.token = token;
  }

  public boolean authenticate(String token) {
    return this.token.equals(token);
  }

}
