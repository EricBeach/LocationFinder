package org.ericbeach.location.services;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import java.util.logging.Logger;

/**
 * Service to handle issues with users.
 */
public class UsersService {
  public static final String NOT_SIGNED_IN_USER = "NOT_SIGNED_IN";
  private static final Logger log =
      Logger.getLogger(UsersService.class.getName());
  private final UserService userService = UserServiceFactory.getUserService();

  public String getCurrentUserEmailAddress() {
    if (userService.isUserLoggedIn()) {
      User user = userService.getCurrentUser();
      String loggedInUserEmailAddress = user.getEmail();
      log.info("Currently signed in user email determined to be: " + loggedInUserEmailAddress);
      return loggedInUserEmailAddress;
    } else {
      return NOT_SIGNED_IN_USER;
    }
  }
}
