MiniRxDemo
==========
Author: Hristo M. Stefanov

## Opening the project in Android Studio

It's best to open the project by using the **File > New > Import project** command.

If the build process fails, try:
* **File > Sync project with gradle files**
* **Build > Rebuild Project**
* **File > Invalidate caches / Restart**


## Tests
The project contains 10 unit tests. To run them in Android Studio create a run 
configuration by navigating to
**Run > Edit Configurations > Add new configuration > Android JUnit**
and set:

* **Name:** All unit tests
* **Test Kind:** All in Package
* **Search for tests:** In whole project

Select the *All unit tests* configuration and run it.





