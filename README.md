Location Finder
==================

___What is Location Finder?___

A web application written with a backend in Java and a front-end in JavaScript that enables approved individuals to:

- Share on a map their office location
- See the location of other individuals
- Receive periodic email updates listing individuals working closeby


___Code Configuration___

The only file you need to change to configure your version of Location Finder is the ```Configuration.java ```file in ```org.ericbeach.location```

Any additional configuration variables necessary for additional features should be specified in the ```Configuration.java ```file.

___Development Environment___

The development environment consists of three parts:

- [Eclipse](https://eclipse.org/) - While you don't technically need this, it integrates very nicely with the Google Plugin for Eclipse, thereby making development much easier than via command-line or another IDE.
- [Google Plugin for Eclipse](https://developers.google.com/eclipse/) - This plugin enables you to run the application locally, deploy to App engine in one-click, and other benefits.
- [Google App Engine SDK](https://cloud.google.com/appengine/downloads) - Download the SDK for Java.

You will need to compile your code for Java 7 by [changing the compliance level](https://developers.google.com/eclipse/docs/jdk_compliance). You can track the status of Google App Engine's support for Java 8 in [this bug](https://code.google.com/p/googleappengine/issues/detail?id=9537). Sadly Java 8 support does not look like it is coming anytime soon.

___Code Style___

- Java - Style conforms to [Google's Java style guide](http://google-styleguide.googlecode.com/svn/trunk/javaguide.html). So, for example, line length should not exceed 100 characters, imports should be ordered such that the project's classes come first then Java classes, no tabs, etc.
- JavaScript -Style conforms to [Google's JavaScript style guide](http://google-styleguide.googlecode.com/svn/trunk/javascriptguide.xml). So, for example, line length should not exceed 80 chars, always declare variables with ```var```, [annotate JavaScript per the Closure Compiler](https://developers.google.com/closure/compiler/docs/js-for-compiler), tend to avoid Closures, define classes via ```prototype```, avoid associative arrays, etc.


___Application Architecture Overview___


- ```org.ericbeach.location.models``` - The application architecture is driven by the datastore models which persist from the back of the stack (Google App Engine datastore) to the front of the stack (JSON objects used in the user interface). The models are stored in this package.

- ```org.ericbeach.location.servlets``` - The servlets control the HTTP requests and HTTP associated responses. In general, the servlets simply dispatch to the services, which then call the datastore.


___Extending App to Support Custom Login___

The code has been built to make it easy to extend, especially when it comes to changing the process for authorizing users and identifying those users.

In short, the application comes with a default set of base classes that implement standard App Engine logic for authorization. The application calls shell subclasses which currently invoke the default set of base classes. However, all you need to do to setup a new login scheme, for example, is change the methods in the subclass to no longer call the default baseclass but instead use your own logic. You will need to keep the method signature the same.

- ``LockoutUnauthorizedUsersService`` - Modify the public methods in this class to change how users are authorized.
- ``UsersService`` - Modify the public methods in this class to change how a user is identified.

If you do wish to change how authentication is done and in particular wish to remove the Google App Engine login, you will need to remove the ```<security-constraint>``` from ```war/WEB-INF/web.xml``` that forces all users to authenticate with Google.


___Security___

The application has been built with security in mind from the ground up. The ```war/WEB-INF/web.xml``` file contains a ```<transport-guarantee>``` that forces SSL for all HTTP connections. Further, all content sits behind a Java class which enables more complicated authentication.

If you add a new servlet, you **must** call the authorization class (``LockoutUnauthorizedUsersService``) in order to ensure that the HTTP request is served only after authorization is performed.

___Open Issues___

- The application does not properly handle the use-case when two individuals work at the exact same location.
- The application does not properly handle the use-case when an individual enters an address that cannot be properly geo-coded.