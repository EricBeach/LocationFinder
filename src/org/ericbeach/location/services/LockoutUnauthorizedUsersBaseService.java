package org.ericbeach.location.services;

import org.ericbeach.location.Configuration;
import org.ericbeach.location.datastore.AuthorizedUserDatastoreHelper;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Base service that locks out unauthorized users.
 */
public abstract class LockoutUnauthorizedUsersBaseService {
  protected static final String PATH_TO_UNAUTHORIZED_PAGE = "/static/unauthorized.html";
  protected static final Logger log =
      Logger.getLogger(LockoutUnauthorizedUsersBaseService.class.getName());
  protected final AuthorizedUserDatastoreHelper authorizedUsersDatastoreHelper =
      new AuthorizedUserDatastoreHelper();
  protected final UserService userService = UserServiceFactory.getUserService();

  protected void lockoutUnauthorizedUsers(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    if (userService.isUserLoggedIn()) {
      User user = userService.getCurrentUser();
      String loggedInUserEmailAddress = user.getEmail();

      if (!isUserAuthorizedToViewApplication(loggedInUserEmailAddress)) {
        log.info("User " + loggedInUserEmailAddress + " is * NOT * authorized to view app");
        resp.sendRedirect(PATH_TO_UNAUTHORIZED_PAGE + "?currentEmailAddress="
            + URLEncoder.encode(loggedInUserEmailAddress, "UTF-8"));
        resp.flushBuffer();
      }
    } else {
      resp.sendRedirect(PATH_TO_UNAUTHORIZED_PAGE);
      resp.flushBuffer();
    }
  }

  protected void lockoutUnauthorizedAdmins(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    if (userService.isUserLoggedIn()) {
      User user = userService.getCurrentUser();
      String loggedInUserEmailAddress = user.getEmail();

      if (!Configuration.AUTHORIZED_ADMIN_EMAIL_ADDRESSES.contains(loggedInUserEmailAddress)
          && !userService.isUserAdmin()) {
        resp.sendRedirect(PATH_TO_UNAUTHORIZED_PAGE);
        resp.flushBuffer();
      }
    } else {
      resp.sendRedirect(PATH_TO_UNAUTHORIZED_PAGE);
      resp.flushBuffer();
    }
  }

  protected boolean isUserAuthorizedToViewApplication(final String userEmailAddress) {
    return (Configuration.AUTHORIZED_ADMIN_EMAIL_ADDRESSES.contains(userEmailAddress)
        || authorizedUsersDatastoreHelper.isEmailAddressInDatabaseOfAuthorizedUsers(
            userEmailAddress) || userService.isUserAdmin());
  }
}
