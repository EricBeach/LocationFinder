package org.ericbeach.location.models;

/**
 * Model for an authorized user.
 */
public class AuthorizedUser {
  private final String email;
  private final String displayName;

  public AuthorizedUser(final String email, final String displayName) {
   this.email = email;
   this.displayName = displayName;
  }

  public String getEmail() {
    return email;
  }

  public String getDisplayName() {
    return displayName;
  }
}
