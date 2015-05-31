# FlickrAndroidDemo
Android Client for Flickr api that lets you search for images. It shows the interesting photos for the day by default.

## Setup
Inside FlickrSettings.java , update the Flickr API key.
```java
public class FlickrSettings {
    public static final String API_KEY = "";
}
```
To run the project
 - Either run via Android Studio
 - cd into the project folder and then issue ``` ./gradlew installDebug```

## Functionality
 - Shows Flickr's interesting photos for the day by default
 - Allows rearranging of pictures via long pressing and drag and drop
 - Tap on the picture to see in larger size
 - one can change orientations

## Libraries Used
 - [Picasso] (https://github.com/square/picasso)
 - [Dynamic Grid] (https://github.com/askerov/DynamicGrid)
 - [Butter Knife] (https://github.com/JakeWharton/butterknife)
 - [Rx Android] (https://github.com/ReactiveX/RxAndroid)

## Screenshots
Screenshots can be found [here](https://www.dropbox.com/sh/bjrg267jkdc6idp/AABZ7gk-OW9RsmxPjBNNF2VSa?dl=0)
