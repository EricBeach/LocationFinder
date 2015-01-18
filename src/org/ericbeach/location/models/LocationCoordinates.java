package org.ericbeach.location.models;

import org.ericbeach.location.datastore.LocationCoordinatesDatastoreHelper;

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

  public String toJson() {
    return LocationCoordinates.toJson(latitude, longitude, locationType, userEmail);
  }

  public boolean equals(LocationCoordinates locationCoordinates) {
    return (this.userEmail.equals(locationCoordinates.getUserEmail())
        && Double.compare(this.latitude, locationCoordinates.getLatitude()) == 0
        && Double.compare(this.longitude, locationCoordinates.getLongitude()) == 0
        && this.locationType == locationCoordinates.getLocationType());
  }

  public static String toJson(final double latitude, final double longitude,
      final int locationType, final String userEmail) {
    String json = "{"
        + "  \"" + LocationCoordinatesDatastoreHelper
            .LOCATION_COORDINATES_USER_EMAIL_PROPERTY_NAME + "\": \"" + userEmail + "\","
        + "  \"" + LocationCoordinatesDatastoreHelper
            .LOCATION_COORDINATES_LOCATION_TYPE_PROPERTY_NAME + "\": " + locationType + ","
        + "  \"" + LocationCoordinatesDatastoreHelper
            .LOCATION_COORDINATES_LATITUDE_PROPERTY_NAME + "\": " + latitude + ","
        + "  \"" + LocationCoordinatesDatastoreHelper
            .LOCATION_COORDINATES_LONGITUDE_PROPERTY_NAME + "\": " + longitude + ""
        + "}";
    return json;
  }
}
