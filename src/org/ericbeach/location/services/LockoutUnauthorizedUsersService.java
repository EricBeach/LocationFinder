package org.ericbeach.location.services;

import org.ericbeach.location.Configuration;
import org.ericbeach.location.datastore.AuthorizedUserDatastoreHelper;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Service that locks out unauthorized users.
 */
public class LockoutUnauthorizedUsersService {
  private static final Logger log =
      Logger.getLogger(LockoutUnauthorizedUsersService.class.getName());
  private final AuthorizedUserDatastoreHelper authorizedUsersDatastoreHelper =
      new AuthorizedUserDatastoreHelper();
  private final UserService userService = UserServiceFactory.getUserService();

  public void lockoutUnauthorizedUsers(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    if (userService.isUserLoggedIn()) {
      User user = userService.getCurrentUser();
      String loggedInUserEmailAddress = user.getEmail();

      if (!isUserAuthorizedToViewApplication(loggedInUserEmailAddress)) {
        log.info("User " + loggedInUserEmailAddress + " is * NOT * authorized to view app");
        resp.sendRedirect("/static/unauthorized.html");
      }
    } else {
      resp.sendRedirect("/static/unauthorized.html");
    }
  }

  public void lockoutUnauthorizedAdmins(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    if (userService.isUserLoggedIn()) {
      User user = userService.getCurrentUser();
      String loggedInUserEmailAddress = user.getEmail();

      if (!Configuration.AUTHORIZED_ADMIN_EMAIL_ADDRESSES.contains(loggedInUserEmailAddress)
          && !userService.isUserAdmin()) {
        resp.sendRedirect("/static/unauthorized.html");
      }
    } else {
      resp.sendRedirect("/static/unauthorized.html");
    }
  }

  public String getCurrentlyLoggedInUserEmailAddress() {
    if (userService.isUserLoggedIn()) {
      User user = userService.getCurrentUser();
      String currentlyLoggedInUserEmailAddress = user.getEmail();
      return currentlyLoggedInUserEmailAddress;
    } else {
      return "";
    }
  }

  private boolean isUserAuthorizedToViewApplication(final String userEmailAddress) {
    return (Configuration.AUTHORIZED_ADMIN_EMAIL_ADDRESSES.contains(userEmailAddress)
        || authorizedUsersDatastoreHelper.isEmailAddressInDatabaseOfAuthorizedUsers(
            userEmailAddress) || userService.isUserAdmin());
  }
}
