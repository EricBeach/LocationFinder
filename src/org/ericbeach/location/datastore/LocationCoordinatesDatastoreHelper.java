package org.ericbeach.location.datastore;

import org.ericbeach.location.models.LocationCoordinates;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Helper for the datastore that fetches authorized location coordinates entity.
 */
public class LocationCoordinatesDatastoreHelper {
  private static final Logger log =
      Logger.getLogger(AuthorizedUserDatastoreHelper.class.getName());

  private static final String LOCATION_COORDINATES_ENTITY_NAME = "location_coordinates";
  public static final String LOCATION_COORDINATES_USER_EMAIL_PROPERTY_NAME = "email";
  public static final String LOCATION_COORDINATES_LATITUDE_PROPERTY_NAME = "latitude";
  public static final String LOCATION_COORDINATES_LONGITUDE_PROPERTY_NAME = "longitude";
  public static final String LOCATION_COORDINATES_LOCATION_TYPE_PROPERTY_NAME = "location_type";

  private final DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();

  public LocationCoordinates updateLocationCoordinatesEntity(
      final String userEmailAddress, final double latitude,
      final double longitude, final int locationType) throws NoSuchEntityException {

    // Remove any existing location coordinate with the same email + type to prevent duplicates.
    Entity deletedLocationCoordinatesEntity =
        deleteLocationCoordinatesEntity(userEmailAddress, locationType);

    log.info("Updating location coordinates for user " + userEmailAddress + " to "
      + " latitude, longitude: " + latitude + ", " + longitude);
    Entity locationEntity = new Entity(LOCATION_COORDINATES_ENTITY_NAME);
    locationEntity.setProperty(LOCATION_COORDINATES_USER_EMAIL_PROPERTY_NAME,
        userEmailAddress);
    locationEntity.setProperty(LOCATION_COORDINATES_LATITUDE_PROPERTY_NAME,
        latitude);
    locationEntity.setProperty(LOCATION_COORDINATES_LONGITUDE_PROPERTY_NAME,
        longitude);
    locationEntity.setProperty(LOCATION_COORDINATES_LOCATION_TYPE_PROPERTY_NAME,
        locationType);
    datastoreService.put(locationEntity);

    return new LocationCoordinates(
        (double) deletedLocationCoordinatesEntity.getProperty(
            LOCATION_COORDINATES_LATITUDE_PROPERTY_NAME),
        (double) deletedLocationCoordinatesEntity.getProperty(
            LOCATION_COORDINATES_LONGITUDE_PROPERTY_NAME),
        (int) (long) deletedLocationCoordinatesEntity.getProperty(
            LOCATION_COORDINATES_LOCATION_TYPE_PROPERTY_NAME),
        (String) deletedLocationCoordinatesEntity.getProperty(
            LOCATION_COORDINATES_USER_EMAIL_PROPERTY_NAME));
  }

  public Entity deleteLocationCoordinatesEntity(final String userEmailAddress,
      final int locationType) throws NoSuchEntityException {
    Entity locationCoordinatesEntity =
        getLocationCoordinatesEntityByUserEmailAndLocationType(userEmailAddress, locationType);
    log.info("Entity found for email " + userEmailAddress + " and location type "
        + locationType + ", proceeding to delete it");
    datastoreService.delete(locationCoordinatesEntity.getKey());
    return locationCoordinatesEntity;
  }

  public LocationCoordinates getLocationCoordinatesByUserEmailAndLocationType(
      final String userEmailAddress, final int locationType) throws NoSuchEntityException {
    Entity locationCoordinates =
        getLocationCoordinatesEntityByUserEmailAndLocationType(userEmailAddress, locationType);

    return new LocationCoordinates(
        (double) locationCoordinates.getProperty(
            LOCATION_COORDINATES_LATITUDE_PROPERTY_NAME),
        (double) locationCoordinates.getProperty(
            LOCATION_COORDINATES_LONGITUDE_PROPERTY_NAME),
        (int) (long) locationCoordinates.getProperty(
            LOCATION_COORDINATES_LOCATION_TYPE_PROPERTY_NAME),
        (String) locationCoordinates.getProperty(
            LOCATION_COORDINATES_USER_EMAIL_PROPERTY_NAME));
  }

  private Entity getLocationCoordinatesEntityByUserEmailAndLocationType(
      final String userEmailAddress, final int locationType) throws NoSuchEntityException {
    Filter userEmailFilter = new FilterPredicate(LOCATION_COORDINATES_USER_EMAIL_PROPERTY_NAME,
        FilterOperator.EQUAL,
        userEmailAddress);
    Filter locationTypeFilter = new FilterPredicate(
        LOCATION_COORDINATES_LOCATION_TYPE_PROPERTY_NAME,
        FilterOperator.EQUAL,
        locationType);

    Filter compositeFilter = new CompositeFilter(CompositeFilterOperator.AND,
        Arrays.asList(userEmailFilter, locationTypeFilter));

    Query query = new Query(LOCATION_COORDINATES_ENTITY_NAME).setFilter(compositeFilter);
    List<Entity> queryResultList =
        datastoreService.prepare(query).asList(FetchOptions.Builder.withLimit(1));

    if (queryResultList.size() == 1) {
      log.info("Entity found for email " + userEmailAddress + " and location type "
          + locationType);
      return queryResultList.get(0);
    } else {
      log.info("NO Entity found for email " + userEmailAddress + " and location type "
          + locationType);
      throw new NoSuchEntityException();
    }
  }

  public List<LocationCoordinates> getAllLocationCoordinates() {
    Query query = new Query(LOCATION_COORDINATES_ENTITY_NAME)
        .addSort(LOCATION_COORDINATES_USER_EMAIL_PROPERTY_NAME, SortDirection.ASCENDING)
        .addSort(LOCATION_COORDINATES_LOCATION_TYPE_PROPERTY_NAME, SortDirection.ASCENDING);
    List<Entity> listOfLocationCoordinates =
        datastoreService.prepare(query).asList(FetchOptions.Builder.withDefaults());

    List<LocationCoordinates> locationCoordinates = new ArrayList<LocationCoordinates>();
    for (Entity locationCoordinatesEntity : listOfLocationCoordinates) {
      locationCoordinates.add(new LocationCoordinates(
          (double) locationCoordinatesEntity.getProperty(
              LOCATION_COORDINATES_LATITUDE_PROPERTY_NAME),
          (double) locationCoordinatesEntity.getProperty(
              LOCATION_COORDINATES_LONGITUDE_PROPERTY_NAME),
          (int) (long) locationCoordinatesEntity.getProperty(
              LOCATION_COORDINATES_LOCATION_TYPE_PROPERTY_NAME),
          (String) locationCoordinatesEntity.getProperty(
              LOCATION_COORDINATES_USER_EMAIL_PROPERTY_NAME)
          ));
    }

    return locationCoordinates;
  }
}
