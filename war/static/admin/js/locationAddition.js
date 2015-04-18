/**
 * @fileoverview Location addition admin functionality.
 */


/**
 * @constructor
 */
LocationAddition = function() {
  /** @private */
  this.formSubmissionElem_ = null;

  /** @private */
  this.checkFormDataElem_ = null;

  /** @private */
  this.isEmailValidSpan_ = null;

  /** @private */
  this.areCoordinatesValidSpan_ = null;

  /** @private */
  this.geocodeBtn_ = null;

  /**
   * @type {!google.maps.Geocoder}
   * @private
   */
  this.geocoder_ = new google.maps.Geocoder();

  /**
   * @private
   */
  this.authorizedUsers_ = {};
};


/**
 * Initialize the location addition functionality.
 */
LocationAddition.prototype.init = function() {
  if (document.getElementById('locationCoordinatesAddBtn') &&
      document.getElementById('locationCoordinatesCheckFormDataBtn') &&
      document.getElementById('isEmailValid') &&
      document.getElementById('areCoordinatesValid') &&
      document.getElementById('geocodeBtn')) {
    this.formSubmissionElem_ =
        document.getElementById('locationCoordinatesAddBtn');
    this.checkFormDataElem_ =
        document.getElementById('locationCoordinatesCheckFormDataBtn');
    this.isEmailValidSpan_ =
        document.getElementById('isEmailValid');
    this.areCoordinatesValidSpan_ =
      document.getElementById('areCoordinatesValid');
    this.geocodeBtn_ = document.getElementById('geocodeBtn');

    this.checkFormDataElem_.addEventListener('click',
        this.handleCheckFormDataElemClick_.bind(this));
    this.geocodeBtn_.addEventListener('click',
        this.handleGeocodeBtnClicked_.bind(this));

    this.loadAuthorizedUsers_();
  }
};


/**
 * @return {Promise}
 * @private
 */
LocationAddition.prototype.loadAuthorizedUsers_ = function() {
  var this_ = this;
  var promise = new Promise(function(resolve, reject) {
    var req = new XMLHttpRequest();
    req.open('GET', '/_/getAuthorizedUsers');

    req.onload = function() {
      if (req.status == 200) {
        this_.authorizedUsers_ = JSON.parse(req.response);
        resolve();
      }
      else {
        reject(Error(req.statusText));
      }
    };

    // Handle network errors
    req.onerror = function() {
      reject(Error('Network Error'));
    };

    // Make the request
    req.send();
  });
  return promise;
};


/**
 * Enable form submission button.
 * @private
 */
LocationAddition.prototype.enableFormSubmission_ = function() {
  this.formSubmissionElem_.removeAttribute('disabled');
}


/**
 * Disable form submission button.
 * @private
 */
LocationAddition.prototype.disableFormSubmission_ = function() {
  this.formSubmissionElem_.addAttribute('disabled');
}


/**
 * Geocode address.
 * @private
 */
LocationAddition.prototype.handleGeocodeBtnClicked_ = function() {
  var address = document.getElementById('location-address').value;
  document.getElementById('notificationText').innerHTML = '';

  var this_ = this;
  this.geocodeAddress_(address)
      .then(this.handleGeocodeAddressResponse_.bind(this),
          function(rejectionReason) {
              document.getElementById('notificationText').innerHTML =
                rejectionReason;
              this_.disableFormSubmission_();
       });
};


/**
 * Handle geocode response.
 * @private
 */
LocationAddition.prototype.handleGeocodeAddressResponse_ = function(resp) {
  if (resp.latitude && resp.longitude) {
    this.enableFormSubmission_();
    document.getElementById('latitude').value = resp.latitude;
    document.getElementById('longitude').value = resp.longitude;
  } else {
    document.getElementById('notificationText').innerHTML = 'Unknown error';
  }
};


/**
 * @param {!string} address
 * @return {Promise}
 * @see https://developers.google.com/maps/documentation/javascript/geocoding
 * @private
 */
LocationAddition.prototype.geocodeAddress_ = function(address) {
  var this_ = this;
  var promise = new Promise(function(resolve, reject) {
    this_.geocoder_.geocode({ 'address': address}, function(results, status) {
      if (status == google.maps.GeocoderStatus.OK) {
        // Note: At times even when you enter a very specific address, Google
        // returns 2 or 3 possible results. The proper way to handle this is to
        // let the user decide which address. Until then, arbitrarily setting
        // cutoff at 4 meaning if Google returns 4 or more addresses, we tell
        // the user to be more specific. If Google returns 2 or less, we
        // simply use the first result.
        if (results.length >= 3) {
          reject('Multiple possible locations found. Please enter a more ' +
              'specific address. For example: 1600 Pennsylvania Ave NW, ' +
              'Washington, DC, 20500');
        } else if (results.length == 0) {
          reject('Unable to find any locations cooresponding to that address.');
        } else if (typeof results[0].geometry.location.lat == 'number' &&
            typeof results[0].geometry.location.lng == 'number') {
          var coordinates = {
            latitude: results[0].geometry.location.lat,
            longitude: results[0].geometry.location.lng
          };
          resolve(coordinates);
        } else if (typeof results[0].geometry.location.D == 'number' &&
                   typeof results[0].geometry.location.k == 'number') {
          var coordinates = {
            latitude: results[0].geometry.location.k,
            longitude: results[0].geometry.location.D
          };
          resolve(coordinates);
        } else {
          // Query executed successfully, but could not pinpoint one address.
          reject('Unable to identify a single specific point on the map.' +
              ' Try a more specific address.');
        }
      } else {
        reject('Geocode was not successful for the following reason: ' +
            status);
      }
    });
  });
  return promise;
};


/**
 * @private
 */
LocationAddition.prototype.handleCheckFormDataElemClick_ = function() {
  var emailAddressToAdd = document.querySelector('input[type="email"]').value;
  if (this.authorizedUsers_[emailAddressToAdd]) {
    this.isEmailValidSpan_.innerHTML = 'yes';
  } else {
    this.isEmailValidSpan_.innerHTML = 'no (email is not in list of authorized app users)';
  }

  var numberElems = document.querySelectorAll('input[type="number"]');
  var areCoordinatesValid = false;
  for (var i = 0; i < numberElems.length; i++) {
    if (numberElems[i].getAttribute('min') &&
        numberElems[i].getAttribute('max')) {
      var maxVal = parseInt(numberElems[i].getAttribute('max'), 10);
      var minVal = parseInt(numberElems[i].getAttribute('min'), 10);
      var val = parseInt(numberElems[i].value, 10);
      if (maxVal >= val && minVal <= val) {
        areCoordinatesValid = true;
      } else {
        areCoordinatesValid = false;
      }
    }
  }

  if (areCoordinatesValid) {
    this.areCoordinatesValidSpan_.innerHTML = 'yes';
  } else {
    this.areCoordinatesValidSpan_.innerHTML = 'no';
  }
};


function init() {
  var locationAddition = new LocationAddition();
  locationAddition.init();
}

document.addEventListener('DOMContentLoaded', init);