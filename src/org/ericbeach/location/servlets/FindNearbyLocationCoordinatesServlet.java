package org.ericbeach.location.servlets;

import org.ericbeach.location.Configuration;
import org.ericbeach.location.datastore.AuthorizedUserDatastoreHelper;
import org.ericbeach.location.models.AuthorizedUser;
import org.ericbeach.location.models.LocationCoordinates;
import org.ericbeach.location.services.NearbyLocationCoordinatesService;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.List;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to handle finding nearby location coordinates.
 */
@SuppressWarnings("serial")
public class FindNearbyLocationCoordinatesServlet extends HttpServlet {
  private static final Logger log =
      Logger.getLogger(FindNearbyLocationCoordinatesServlet.class.getName());
  private NearbyLocationCoordinatesService findNearbyLocationCoordinatesService =
      new NearbyLocationCoordinatesService();
  private AuthorizedUserDatastoreHelper authorizedUserDatastoreHelper =
      new AuthorizedUserDatastoreHelper();

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    double distanceInMilesConsideredCloseby = 0.5;
    // STEP 1: Get All Authorized Users
    List<AuthorizedUser> authorizedUsers =
        authorizedUserDatastoreHelper.getAllAuthorizedUserObjects();

    for (AuthorizedUser authorizedUser : authorizedUsers) {
      // STEP 2: For Each Authorized User, Look for Nearby Location Coordinates
      List<LocationCoordinates> nearbyLocationCoordinates =
          findNearbyLocationCoordinatesService.getNearbyLocationCoordinates(
              authorizedUser.getEmail(), distanceInMilesConsideredCloseby);
      log.info("For user " + authorizedUser.getEmail() + " " + nearbyLocationCoordinates.size()
          + " nearby location coordinates found");

      // STEP 3: Email Authorized User Email + Name of Nearby Location Coordinates
      if (nearbyLocationCoordinates.size() > 0) {
        emailListOfNearbyLocationCoordinates(authorizedUser, nearbyLocationCoordinates,
            distanceInMilesConsideredCloseby);
      }
    }

    // Add HTTP response header to help prevent clickjacking.
    resp.addHeader("X-Frame-Options", "SAMEORIGIN");
    resp.getWriter().println("Finished");
  }

  private boolean emailListOfNearbyLocationCoordinates(AuthorizedUser authorizedUser,
      List<LocationCoordinates> nearbyLocationCoordinates,
      double distanceInMilesConsideredCloseby) {
    Properties props = new Properties();
    Session session = Session.getDefaultInstance(props, null);

    String msgBody = "The following individuals are within "
        + Double.toString(distanceInMilesConsideredCloseby) + " miles of you.\n\n";
    for (LocationCoordinates nearbyLocationCoordinate : nearbyLocationCoordinates) {
      msgBody += nearbyLocationCoordinate.getUserEmail() + "\n";
    }

    msgBody += "\nPlease visit the " + Configuration.WEBSITE_TITLE + " website to see a map.";

    try {
      Message msg = new MimeMessage(session);
      msg.setFrom(new InternetAddress(Configuration.FROM_EMAIL_ADDRESS,
          Configuration.FROM_EMAIL_NAME));
      msg.addRecipient(Message.RecipientType.TO,
          new InternetAddress(authorizedUser.getEmail(), authorizedUser.getDisplayName()));
      msg.setSubject(Configuration.EMAIL_REPORT_SUBJECT_LINE);
      msg.setText(msgBody);
      Transport.send(msg);
    } catch (UnsupportedEncodingException e) {
      log.severe("UnsupportedEncodingException when attempting to email");
      return false;
    } catch (AddressException e) {
      log.severe("AddressException when attempting to email");
      return false;
    } catch (MessagingException e) {
      log.severe("MessagingException when attempting to email");
      return false;
    }
    return true;
  }
}
