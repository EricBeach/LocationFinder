/**
 * @fileoverview Bulk location addition admin functionality.
 */



/**
 * @constructor
 */
BulkLocationAddition = function() {
  /** @private */
  this.dataInputModeElem_ = null;

  /** @private */
  this.formatExampleElem_ = null;

  /** @private */
  this.formatDescriptionElem_ = null;
};


/**
 * Initialize the bulk location addition functionality.
 */
BulkLocationAddition.prototype.init = function() {
  if (document.getElementById('bulkAddLocationDataInputModeSelector') &&
      document.getElementById('formatDescription') &&
      document.getElementById('formatExample')) {
    this.dataInputModeElem_ =
        document.getElementById('bulkAddLocationDataInputModeSelector');
    this.formatDescriptionElem_ =
        document.getElementById('formatDescription');
    this.formatExampleElem_ =
        document.getElementById('formatExample');
    this.dataInputModeElem_.addEventListener('change',
        this.handleDataInputModeChange_.bind(this));
  }
};


/**
 * Handle value changing for data input type.
 * @private
 */
BulkLocationAddition.prototype.handleDataInputModeChange_ = function() {
  var dataInutMode = this.dataInputModeElem_.options[
      this.dataInputModeElem_.selectedIndex].value;

  if (dataInutMode == 0) {
    this.formatExampleElem_.innerHTML =
        'John Doe, user@domain.com';
    this.formatDescriptionElem_.innerHTML =
        'display name, email address';
  } else if (dataInutMode == 1) {
    this.formatExampleElem_.innerHTML =
        'user@domain.com, 38.900996, -77.017605, 0';
    this.formatDescriptionElem_.innerHTML =
        'email address, lat, long, location type';
  } else if (dataInutMode == 2) {
    this.formatExampleElem_.innerHTML =
        'John Doe, user@domain.com, 38.900996, -77.017605, 0';
    this.formatDescriptionElem_.innerHTML =
        'display name, email address, lat, long, location type';
  }
};


function init() {
  var bulkLocationAddition = new BulkLocationAddition();
  window.setTimeout(function() {
    bulkLocationAddition.init();
  }, 100);
}

document.addEventListener('DOMContentLoaded', init);
