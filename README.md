# PHOTOFY

## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)

## Overview
### Description
Photofy allows you to take a picture of something around you, pulls the most dominant colors, and based on the mood associated with the colors, generates a song for you. You can then share the picture and song with others through the app as well as discover new songs by viewing images on a feed, similar to a social media platform.

### App Evaluation
- **Category:** Photo, Music, Social Networking
- **Mobile:** Pretty uniquely mobile. The app integrates a camera that allows users to take a picture and get a song on the spot, which is much easier in the app than on a website.
- **Story:** High value to the audience/young adults since people are constantly trying to find new music to listen to. Through the app you can post your own images and songs or view the posts of others. Friends and peers would respond positively to this app idea since it allows them to share parts of their lives with others through images as well as gives them associated songs to listen to.
- **Market:** Anyone who wants to discover new music and can take photos. Provides value to a large group of people since most people listen to music, have a camera, and like to share photos with others. There is not a clear well-defined audience for the app but most common users are young adults since they use social networking apps the most often.
- **Habit:** The app is pretty habit forming or addicting since the user can consistently take photos and discover new music. Even if they are in the same location all day, depending on the photo they take, they can get a different song. An average user can open and use this app likely everyday to see the posts of others and would both create and consume content in the app.
- **Scope:** I believe the app would be pretty technically challenging to build since it involves using 2 APIs: Google Cloud Vision API and Spotify API. Further, the connection between getting a color to a mood to a genre and finally, to a song might be difficult. However, I believe this app is definitely interesting and could be stripped-down and simplified as needed.

## Product Spec

### 1. User Stories (Required and Optional)

**Required Must-have Stories**

* Users can take images within the app
* User can upload an image from their camera roll to get a song
* App will automatically generate a song from an image
* App lets the user preview the song within the app (30 second spotify preview)
* User can swipe left/right to the generated song with right implying the user likes the song and left implying the user doesn't so the app would generate a new song from the same image
* The image and generated song will be saved in the user's history 
* User can post the new image and song to their feed
* User can view a feed of posts
* User can sign up for a new account
* User can login/logout
* User can view their profile and image/song history

**Optional Nice-to-have Stories**

* Users can set their preferred music genres
* Options to customize the post (different templates for the image/song, background color, text color)
* User can follow/unfollow another user
* Users can like a post
* User can comment on a post
* User can view an explore page with music similar to music that they generated
* User can see notifications when their photo is liked, commented or they are followed
* User can see a list of their followers
* User can see a list of their following

### 2. Screen Archetypes

* Login screen
   * User can login/logout
* Registration screen
   * User can sign up for a new account
* Stream
    * User can view a feed of posts
* Creation
    * Users can take images within the app
    * User can upload an image from their camera roll to get a song
    * App will automatically generate a song from an image
    * App lets the user preview the song within the app (15 second spotify preview)
    * User can swipe left/right to the generated song with right implying the user likes the song and left implying the user doesn't so the app would generate a new song from the same image
    * The image and generated song will be saved in the user's history
    * User can post the new image and song to their feed
* Profile
    * User can view their profile and image/song history

### 3. Navigation

**Tab Navigation** (Tab to Screen)

* Home feed
* Take image and generate a song
* User profile

**Flow Navigation** (Screen to Screen)

* Login screen
   * Home
* Registration screen
   * Home
* Stream
    * None, but future version will likely involve navigation to a detailed screen to see comments for each post
* Creation
    * Home (after you finish generating a song and posting)
    * Multiple screens needed to represent the creation process (picture loading, generated song pops up before the image and song are put together in a post)
* Profile
    * None, but future version will likely involve navigation to see following/followers

## Wireframes
![image](https://user-images.githubusercontent.com/85804507/173662555-8fac91fd-7db4-49c1-970a-27e4898990b5.png)

### [BONUS] Digital Wireframes & Mockups

### [BONUS] Interactive Prototype

## Schema 
### Models
#### Image
   | Property      | Type     | Description |
   | ------------- | -------- | ------------|
   | objectId      | String   | unique id for the image (default field) |
   | image         | File     | image that user takes|
   | color         | Array    | dominant color associated with the image |

#### Song
   | Property      | Type     | Description |
   | ------------- | -------- | ------------|
   | objectId      | String   | unique id for the song (default field) |
   | spotifyId     | String   | spotify id for the song, gives URL |
   | name          | String   | name of the song |
   | artist        | String   | artist of the song |
   | genres        | Array    | genres associated with the song |
   | preview       | String   | 30 second song preview URL |

#### Post
   | Property      | Type     | Description |
   | ------------- | -------- | ------------|
   | objectId      | String   | unique id for the user post (default field) |
   | user          | Pointer to User  | post author |
   | image         | Pointer to Image | photo that user takes|
   | song          | Pointer to Song | song that user generates |
   | caption       | String   | post caption by author |
   | createdAt     | DateTime | date when post is created (default field) |
   
#### User
   | Property      | Type     | Description |
   | ------------- | -------- | ------------|
   | objectId      | String   | unique id for the user (default field) |
   | profileImage  | File     | profile picture for the user
   | biography     | String   | bio for the user |
   
### Networking
#### List of network requests by screen
   - Login Screen
      - (Read/GET) User log in
      - (Create/POST) New user sign up
   - Home Feed Screen
      - (Read/GET) Query all posts
   - Generate song Screen
      - (Create/POST) Create a new image object
      - (Create/POST) Create a new song object (get song from Spotify)
   - Profile Screen
      - (Read/GET) Query logged in user object
      - (Read/GET) Query all past images and songs where user is the author

#### List endpoints if using existing API
##### Google Cloud Vision API
- Base URL - [https://vision.googleapis.com/v1](https://vision.googleapis.com/v1)

   HTTP Verb | Endpoint | Description
   ----------|----------|------------
    `POST`    | /images:annotate | annotate image to get dominant colors

##### Spotify API
- Base URL - [https://api.spotify.com/v1](https://api.spotify.com/v1)

   HTTP Verb | Endpoint | Description
   ----------|----------|------------
    `GET`    | /recommendations | gets all song recommendations
    `GET`    | /recommendations/available-genre-seeds | gets a list of available genres seed parameter values for recommendations
    `GET`    | /tracks/:id  | gets song and associated info (name, artist, album) by spotify id
