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
        + "    <script src=\"https://maps.googleapis.com/maps/api/js?v=3.exp\""
        + "      type=\"text/javascript\"></script>"
        + "    <script src=\"/static/admin/js/locationAddition.js\" type=\"text/javascript\"></script>"
        + "    <title>" + Configuration.WEBSITE_TITLE + " - Admin</title>"
        + "  </head>"
        + "  <body class=\"admin-page\">"
        + "  <p><a href=\"/admin/user#add-user\">Add User</a> &middot; "
        + "    <a href=\"/admin/user#remove-user\">Remove User</a> &middot; "
        + "    <a href=\"/admin/location#add-location\">Add Location Coordinates</a> &middot; "
        + "    <a href=\"/admin/view-locations#view-locations\">View Location Coordinates</a></p>";
    return htmlContents;
  }

  public static String getHtmlForBottomOfAdminPages() {
    String htmlContents = "  </body>"
      + "</html>";
    return htmlContents;
  }
}
