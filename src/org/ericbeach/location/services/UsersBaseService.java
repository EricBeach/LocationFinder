package org.ericbeach.location.services;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import java.util.logging.Logger;

/**
 * Service to handle issues with users.
 */
public abstract class UsersBaseService {
  public static final String NOT_SIGNED_IN_USER = "NOT_SIGNED_IN";
  protected static final Logger log =
      Logger.getLogger(UsersBaseService.class.getName());
  protected final UserService userService = UserServiceFactory.getUserService();

  protected String getCurrentUserEmailAddress() {
    if (userService.isUserLoggedIn()) {
      User user = userService.getCurrentUser();
      String loggedInUserEmailAddress = user.getEmail();
      log.info("Currently signed in user email determined to be: " + loggedInUserEmailAddress);
      return loggedInUserEmailAddress.toLowerCase();
    } else {
      return NOT_SIGNED_IN_USER;
    }
  }
}
