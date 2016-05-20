package org.ericbeach.location;

import java.util.HashSet;
import java.util.Set;

/**
 * NOTE: This is the only file you should need to change.
 */
public class Configuration {
  public static final String WEBSITE_TITLE = "Location Finder";
  public static final String FROM_EMAIL_ADDRESS = "";
  public static final String FROM_EMAIL_NAME = "Location Finder";
  public static final String EMAIL_REPORT_SUBJECT_LINE = "Report of People Working Nearby";

  // See https://developers.google.com/maps/documentation/javascript/get-api-key#get-an-api-key
  // Found under Cloud Console -> API Manager -> Credentials
  public static final String GOOGLE_MAPS_API_KEY = "";

  public static final Set<String> AUTHORIZED_ADMIN_EMAIL_ADDRESSES = new HashSet<String>();
  static {
    // AUTHORIZED_ADMIN_EMAIL_ADDRESSES.add("my.email.account@gmail.com");
  }
}
