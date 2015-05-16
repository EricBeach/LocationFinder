function getParameterByName(name) {
  name = name.replace(/[\[]/, '\\[').replace(/[\]]/, '\\]');
  var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
      results = regex.exec(location.search);
  return (results === null ? '' : decodeURIComponent(
      results[1].replace(/\+/g, ' ')));
}

function populateCurrentEmailAddress() {
  var currentEmailAddress = getParameterByName('currentEmailAddress');
  if (currentEmailAddress && currentEmailAddress.length > 0) {
    document.getElementById('email-login-account').innerHTML =
        '(<b>' + currentEmailAddress + '</b>)';
  }
}

document.addEventListener('DOMContentLoaded', populateCurrentEmailAddress);
