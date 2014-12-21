package org.ericbeach.location.models;

/**
 * Model for a location type.
 */
public class LocationType {
  // NOTE: Never ever remove/edit these values if data exists in the datastore. 
  public static final int OFFICE = 0;
  public static final int HOME = 1;

  public static int parseLocationType(String locationType) {
    if (locationType.equals(String.valueOf(HOME))) {
      return HOME;
    }

    // Default to OFFICE.
    return OFFICE;
  }
}
