package org.ericbeach.location.datastore;

import org.ericbeach.location.models.AuthorizedUser;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Helper for the datastore that fetches authorized user entities.
 */
public class AuthorizedUserDatastoreHelper {
  private static final Logger log =
      Logger.getLogger(AuthorizedUserDatastoreHelper.class.getName());

  private static final String AUTHORIZED_USER_ENTITY_NAME = "authorized_user";
  public static final String AUTHORIZED_USER_EMAIL_PROPERTY_NAME = "email";
  public static final String AUTHORIZED_USER_DISPLAY_NAME_PROPERTY_NAME = "display_name";

  private final DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();

  public void addAuthorizedUser(final String userEmailAddress, final String displayName) {
    // Remove any user with the existing email address to prevent duplicates.
    removeAuthorizedUser(userEmailAddress);
    
    Entity authorizedUserEntity = new Entity(AUTHORIZED_USER_ENTITY_NAME);
    authorizedUserEntity.setProperty(AUTHORIZED_USER_EMAIL_PROPERTY_NAME,
        userEmailAddress);
    authorizedUserEntity.setProperty(AUTHORIZED_USER_DISPLAY_NAME_PROPERTY_NAME,
        displayName);
    Key userKey = datastoreService.put(authorizedUserEntity);
    log.info("Successfully added user " + userEmailAddress + " -- Key: " + userKey.getId());
  }

  public void removeAuthorizedUser(final String userEmailAddress) {    
    try {
      Entity authorizedUserEntity = getAuthorizedUserEntityByEmailAddress(userEmailAddress);
      datastoreService.delete(authorizedUserEntity.getKey());
      log.info("Successfully deleted user " + userEmailAddress + " -- Key: "
          + authorizedUserEntity.getKey().getId());
    } catch (NoSuchEntityException e) {
    }

    // TODO: Delete all LocationCoordinates entities associated with this email address.
  }

  public List<String> getAllAuthorizedUserEmailAddresses() {
    Query query = new Query(AUTHORIZED_USER_ENTITY_NAME);
    List<Entity> listOfAuthorizedUserEntities =
        datastoreService.prepare(query).asList(FetchOptions.Builder.withDefaults());

    List<String> listOfAdminUsers = new ArrayList<String>();
    for (Entity adminUserEntity : listOfAuthorizedUserEntities) {
      listOfAdminUsers.add((String) adminUserEntity.getProperty(
          AUTHORIZED_USER_EMAIL_PROPERTY_NAME));
    }

    log.info("Successfully fetched list of authorized " + listOfAuthorizedUserEntities.size()
        + " users");
    return listOfAdminUsers;
  }

  public List<AuthorizedUser> getAllAuthorizedUserObjects() {
    Query query = new Query(AUTHORIZED_USER_ENTITY_NAME);
    List<Entity> listOfAuthorizedUserEntities =
        datastoreService.prepare(query).asList(FetchOptions.Builder.withDefaults());

    List<AuthorizedUser> listOfAuthorizedUserObjects = new ArrayList<AuthorizedUser>();
    for (Entity authorizedUserEntity : listOfAuthorizedUserEntities) {
      AuthorizedUser authorizedUser = new AuthorizedUser(
          (String) authorizedUserEntity.getProperty(AUTHORIZED_USER_EMAIL_PROPERTY_NAME),
          (String) authorizedUserEntity.getProperty(AUTHORIZED_USER_DISPLAY_NAME_PROPERTY_NAME));
      listOfAuthorizedUserObjects.add(authorizedUser);
    }

    return listOfAuthorizedUserObjects;
  }

  public boolean isEmailAddressInDatabaseOfAuthorizedUsers(final String userEmailAddress) {
    try {
      Entity authorizedUserEntity = getAuthorizedUserEntityByEmailAddress(userEmailAddress);
      log.info("Query for user " + userEmailAddress + " returned verdict: "
          + ((authorizedUserEntity != null) ? "access" : "no access"));
      return true;
    } catch (NoSuchEntityException e) {
      return false;
    }
  }

  private Entity getAuthorizedUserEntityByEmailAddress(final String userEmailAddress)
      throws NoSuchEntityException {
    String userEmailAddressToCheck = userEmailAddress.toLowerCase();
    Filter propertyFilter = new FilterPredicate(
        AUTHORIZED_USER_EMAIL_PROPERTY_NAME,
        FilterOperator.EQUAL,
        userEmailAddressToCheck);
    Query query = new Query(AUTHORIZED_USER_ENTITY_NAME)
        .setFilter(propertyFilter);
    List<Entity> queryResultList =
        datastoreService.prepare(query).asList(FetchOptions.Builder.withLimit(1));

    if (queryResultList.size() == 1) {
      return queryResultList.get(0);
    } else {
      throw new NoSuchEntityException();
    }
  }
}
