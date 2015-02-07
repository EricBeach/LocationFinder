package org.ericbeach.location.services;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Service that locks out unauthorized users.
 * Any custom implementation of locking out unauthorized users can be done here.
 */
public class LockoutUnauthorizedUsersService extends LockoutUnauthorizedUsersBaseService {

  @Override
  public void lockoutUnauthorizedUsers(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    super.lockoutUnauthorizedUsers(req, resp);
  }

  @Override
  public void lockoutUnauthorizedAdmins(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    super.lockoutUnauthorizedAdmins(req, resp);
  }
}
