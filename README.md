MiniRxDemo
==========
Author: Hristo M. Stefanov

This Android app is a demo and is not intended for actual use. It demos
using RxJava in all layers of the app.

The only screen of the app lists the 10 first posts from the fake JSONPlaceholder API. In
order to display the username along each post, request chaining is used.

Refreshing the list of posts from the remote data source is demonstrated too.

## Opening the project in Android Studio

It's best to open the project by using the **File > New > Import project** command.

If the build process fails, try:
* **File > Sync project with gradle files**
* **Build > Rebuild Project**
* **File > Invalidate caches / Restart**

## Architecture
The author aims to follow the ideas in the "*Clean architecture*" book by
Robert C. Martin.
