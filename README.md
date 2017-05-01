Loco
======
![Loco logo](documentation/loco_logo.png)

Loco is a location-driven app that allows users to check or share their pictures, messages, or even happiness level at a specific location.

## Current Progress
The app is fully functional.

## User Interface (Activities)
* User authentication (sign in/sign up/reset)
  * LoginActivity
  * SignupActivity
  * ResetPasswordActivity
* Discover: displays things shared(pictures/posts/happiness rating) at a specific location
  * MainActivity
  * PostsActivity
  * PhotosActivity (PhotoFullSizeActivity)
* Share: allows user to share pictures and posts about a specific location
  * ShareTextActivity
* Profile: displays user's info and options to change email/password
  * ProfileActivity
  * ChangeProfileActivity
		
## Optional Features
* Firebase Authentication
* Firebase Realtime Database for storing user info and location info
* Firebase Storage for storing large user-generated content
* Location service and geocoding through Google Maps API
* External camera for taking picture
* Glide API for loading pictures from Firebase storage

## Testing
During the early stage of development, we mainly used testing activities along with local assets to test functionalities of our app.
* [LocationActivity](https://github.com/fairbet/loco/blob/94d1bb364601c0fa5d556486ca1ab23018d2ab6a/app/src/main/java/android/duke290/com/loco/LocationService.java)
* [DatabaseTestingActivity](https://github.com/fairbet/loco/blob/bc24957b3f9b5c7faf7d4ec7a37e688a16f55458/app/src/main/java/android/duke290/com/loco/DatabaseTestingActivity.java)

After beta version, we tested the following features to ensure standard applicaiton operation.
* Changing Orientation:
	* Main UI: 
		* slight lag sometimes, but no crashes (indicating that the current location was restored correctly)
		* if location is still being calculated, changing orientation doesn't crash the app (indicating that the current location isn't attempted to be accessed when it doesn't exist)
	* Photos/Posts UI: no lag at all, no crashes (indciating Glide's caching works correctly)
	* Profile: no lag at all, no crashes
* Changing Location:
	* Main UI refreshes without retaining any elements from the previous location (indicating that location, photos, posts, address, ratings are correctly refreshed)
	* Main UI doesn't constantly refresh when location barely changes (indicating that the LocationService correctly ignores location changes if the location doesn't change much (as judged by the CLOSE_DISTANCE global var in LocationService))
* Location accuracy:
	* Using Google's Fused Api for finding location coordinates gives reasonable accuracy most times (< 50 meters), but sometimes gives bad accuracy (~500 meters sometimes), but this is less our app's fault and more the strength of the phone's wifi/cellular signal
* Login/Sign-in:
	* Firebase Authentication correctly contains all Loco users, and Firebase Database is correctly updated for all Loco users
* Posting content:
	* Photos are correctly stored in Firebase Storage and posts are correctly stored in the Creations section of Firebase Database
	* Ratings are correctly stored in Firebase Database as "rating" type Creations
	* Rating calculations (average, total num) are correct
	* UI correctly refreshes as soon as something is posted
* Looking at user content:
	* All RecyclerViews (for photos/posts) scroll properly and display content without weird scaling issues/deformations
	* "VIEW ALL" option for posts/photos correctly displays all posts/photos made at the current location
	
## Past versions
##### Functional as of 4/25 (beta)
Basic functionality has been achieved.
* User interface
    * User authentication (sign in/sign up/reset)
	* Main: displays things shared at a specific location
	* Share: allows user to share pictures and messages about a specific location
    * Profile: displays user's name as well as things that user shared
* Features
    * Location service and geocoding through Google Maps API
    * External camera for taking pictures
    * Firebase Authentication
    * Firebase Realtime Database for storing user info and location info
    * Firebase Storage for storing large user-generated content
	* Glide API for loading pictures from Firebase storage


##### Functional as of 4/17 (alpha)
* User interface
    * User authentication (sign in/sign up/reset)
    * Basic profile
* Features
    * Firebase user authentication
    * Firebase realtime database for storing user info and location info
    * Firebase storage for storing large user-generated content
    * Location service and geocoding
    * External camera for taking pictures

## Contributors
* Kevin Kuo
* Fred Xu
* Jihane Bettahi
* Wendy Lau

