package org.ericbeach.location.servlets.admin;

import org.ericbeach.location.Configuration;

/**
 * Common methods for admin pages.
 */
public class CommonAdmin {
  public static String getHtmlForTopOfAdminPages() {
    String htmlContents = "<!DOCTYPE html>"
        + "  <head>"
        + "    <meta name=\"viewport\" content=\"initial-scale=1.0, user-scalable=no\">"
        + "    <link rel=\"stylesheet\" type=\"text/css\" href=\"/static/css/style.css\">"
        + "    <title>" + Configuration.WEBSITE_TITLE + " - Admin</title>"
        + "  </head>"
        + "  <body class=\"admin-page\">"
        + "  <p><a href=\"/admin#add-user\">Add User</a> &middot; "
        + "    <a href=\"/admin#remove-user\">Remove User</a></p>";
    return htmlContents;
  }

  public static String getHtmlForBottomOfAdminPages() {
    String htmlContents = "  </body>"
      + "</html>";
    return htmlContents;
  }
}
