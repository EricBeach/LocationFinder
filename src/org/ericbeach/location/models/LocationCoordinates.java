package org.ericbeach.location.models;

/**
 * Model for a location coordinates.
 */
public class LocationCoordinates {
  private final double latitude;
  private final double longitude;
  private final int locationType;
  private final String userEmail;

  public LocationCoordinates(final double latitude, final double longitude,
      final int locationType, final String userEmail) {
    this.latitude = latitude;
    this.longitude = longitude;
    this.locationType = locationType;
    this.userEmail = userEmail;
  }

  public String getUserEmail() {
    return userEmail;
  }

  public int getLocationType() {
    return locationType;
  }

  public double getLongitude() {
    return longitude;
  }

  public double getLatitude() {
    return latitude;
  }
}
