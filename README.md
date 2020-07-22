MiniRxDemo
==========
Author: Hristo M. Stefanov

This Android app is a demo and is not intended for actual use. It demos
using RxJava in all layers of the app.

## Features
* Displays a list of mock posts from the JSONPlaceholder API 
* Offline mode (with a local DB)
* Auto-refreshes local data
* Swipe-to-refresh gesture refreshes local data

## Opening the project in Android Studio

It's best to open the project by using the **File > New > Import project** command.

If the build process fails, try:
* **File > Sync project with gradle files**
* **Build > Rebuild Project**
* **File > Invalidate caches / Restart**

## Architecture
The author aims to follow the ideas in the "*Clean architecture*" book by
Robert C. Martin.
