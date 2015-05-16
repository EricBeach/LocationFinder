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

  /** @private */
  this.authorizedUsers_ = {};

  /**
   * @type {GeocodeHelper}
   * @private
   */
  this.geocodeHelper_ = new GeocodeHelper();
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
};


/**
 * Disable form submission button.
 * @private
 */
LocationAddition.prototype.disableFormSubmission_ = function() {
  this.formSubmissionElem_.setAttribute('disabled', 'disabled');
};


/**
 * Geocode address.
 * @private
 */
LocationAddition.prototype.handleGeocodeBtnClicked_ = function() {
  var address = document.getElementById('location-address').value;
  document.getElementById('notificationText').innerHTML = '';

  var this_ = this;
  this.geocodeHelper_.geocodeAddress(address)
      .then(this.handleGeocodeAddressResponse_.bind(this),
          function(rejectionReason) {
              document.getElementById('notificationText').innerHTML =
                rejectionReason;
              this_.disableFormSubmission_.call(this_);
      });
};


/**
 * Handle geocode response.
 * @param {Object} resp Geocode response object from Maps API.
 * @private
 */
LocationAddition.prototype.handleGeocodeAddressResponse_ = function(resp) {
  if (resp.latitude && resp.longitude) {
    this.handleCheckFormDataElemClick_();
    document.getElementById('latitude').value = resp.latitude;
    document.getElementById('longitude').value = resp.longitude;
  } else {
    document.getElementById('notificationText').innerHTML = 'Unknown error';
  }
};


/**
 * @private
 */
LocationAddition.prototype.handleCheckFormDataElemClick_ = function() {
  var emailAddressToAdd = document.querySelector('input[type="email"]').value;
  var isAuthorizedUser = false;
  if (this.authorizedUsers_[emailAddressToAdd]) {
    this.isEmailValidSpan_.innerHTML = 'yes';
    isAuthorizedUser = true;
  } else {
    this.isEmailValidSpan_.innerHTML =
        'no (email is not in list of authorized app users)';
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

  if (areCoordinatesValid && isAuthorizedUser) {
    this.enableFormSubmission_();
  } else {
    this.disableFormSubmission_();
  }
};


function init() {
  var locationAddition = new LocationAddition();
  locationAddition.init();
}

document.addEventListener('DOMContentLoaded', init);
