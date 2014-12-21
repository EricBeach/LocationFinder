package org.ericbeach.location;

import java.util.HashSet;
import java.util.Set;

/**
 * NOTE: This is the only file you should need to change.
 */
public class Configuration {
  public static final String WEBSITE_TITLE = "Location Finder";

  public static final Set<String> AUTHORIZED_ADMIN_EMAIL_ADDRESSES = new HashSet<String>();
  static {
    // AUTHORIZED_ADMIN_EMAIL_ADDRESSES.add("my.email.account@gmail.com");
  }
}
