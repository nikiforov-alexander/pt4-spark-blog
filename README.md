## Techdegree project 4
### Blog with Spark
<hr>
### Table of Contents
### Installation instructions
* [Eclipse installation instructions.] (#eclipse)
<hr>

### Tasks
* [1.] (#task-1) Use supplied mockup files to build personal blog.
* [2.] (#task-2) In IntelliJ IDEA, create a Gradle project. Add all 
        required Spark dependencies, and create the directory and package 
        structure of the application. Save all static assets into the 
        proper directory.
* [3.] (#task-3) Create model classes for blog entries and blog 
    comments.
* [4.] (#task-4) Create a DAO interface for data storage and access and 
    implement it.
* [5.] (#task-5) Add necessary routes.
* [6.] (#task-6) Create index view as the homepage. This view contains 
    a list of blog entries, which displays Title, Date/Time Created. 
    Title should be hyperlinked to the detail page for each blog entry. 
    Include a link to add an entry. 
* [7.] (#task-7) Create detail page displaying blog entry and submitted 
    comments. Detail page should also display a comment form with Name 
    and Comment. Include a link to edit the entry.
* [8.] (#task-8) Create a password page that requires a user to 
    provide 'admin' as the username. This page should display before 
    adding or editing a comment if there is no cookie present that has 
    the value 'admin'.
* [9.] (#task-9) Create add/edit page that is blocked by a password 
    page, using before filter.
* [10.] (#task-10) Use CSS to style headings, font colors, blog entry 
    container colors, body colors.
### Extra Credit
* [11.] (#task-11)
    Add tags to blog entries, which enables the ability to categorize. 
    A blog entry can exist with no tags, or have multiple tags.
* [12.] (#task-12)
    Add the ability to delete a blog entry.
* [13.] (#task-13)
    Issue a cookie upon entering the password, and check for it upon 
    adding or editing a post.
 

[resources]:src/main/resources 
[templates]:src/main/resources/templates
[css]:src/main/resources/public/css 
[site.css]:src/main/resources/public/css/site.css 
[BlogEntry]:src/main/java/com/teamtreehouse/blog/model/BlogEntry.java 
[Comment]:src/main/java/com/teamtreehouse/blog/model/Comment.java 
[Date]:src/main/java/com/teamtreehouse/blog/model/Date.java 
[BlogDao]:src/main/java/com/teamtreehouse/blog/dao/BlogDao.java
[Sql2oBlogDao]:src/main/java/com/teamtreehouse/blog/dao/Sql2oBlogDao.java 
[NotFoundException]:src/main/java/com/teamtreehouse/blog/exception/NotFoundException.java 
### Eclipse Installation instructions
<hr> <a id="eclipse"></a>
    I generated necessary `.classpath`, `.project` and 
    `blog.userlibraires`
    files with IntellijIdea. I also made a video how I imported my
    project to my home Kubuntu 14.04 laptop with Eclipse Mars 1:
    https://www.youtube.com/watch?v=7ygSsvATNpQ
    I will provide instructions here, and hopefully with this video
    and instructions it will be possible to import project to Mac 
    Eclipse. NOTE: I have gradle buildship plugin installed in Eclipse: 
    
1.  In opened Eclipse app, I go File -> Import -> Gradle Project
2.  Pick up a project downloaded from GitHub
3.  Follow default necessary steps until the end.
4.  I also make sure Gradle is using right JDK.
5.  Then I configure Source folders on build path, leaving 
    only two, see video.
6.  Then I uncomment `apply plugin 'eclipse'` in `build.gradle`
7.  After that I go to project root directory, make `gradlew` executable
8.  Run `gradlew clean`, `gradlew eclipse`
9.  After that I go to properties of project, to Build Path
10. There I mark select all, to import all jars from dependencies in
    Gradle
11. Say yes to warning.
12. Then run Main method.

<hr>
### Tasks
1.  <a id="task-1"></a>
    Use the supplied mockup files to build a personal blog.
    <hr>
    All 5 html files were used to create according .hbs files. CSS files 
    are used as well, with some tiny changes, like adding class for tag 
    and remove button.
<hr>
2.  <a id="task-2"></a>
    In IntelliJ IDEA, create a Gradle project. Add all required Spark 
    dependencies, and create the directory and package structure of the 
    application. Save all static assets into the proper directory.
    <hr>
    Gradle project created with all spark dependencies. Resources
    directory [resources] is created and marked as dir. 
    CSS files are in [css] dir.
    Template .hbs files are in [templates] dir.
<hr>
3.  <a id="task-3"></a>
    Create model classes for blog entries and blog comments. 
    <hr>
    Model classes are:
    - [BlogEntry]
    - [Comment]
    
    I also added [Date] class, just for convenience, that inherits 
    `java.util.Date` class, but has two additional methods, that return
    date in right machine-readable format for html datetime element: 
    `getHtmlCreationDate()` 
    and return date in format provided in mockup files for users to see
    `getCreationDateString()`
<hr>
4.  <a id="task-4"></a>
    Create a DAO interface for data storage and access and implement it.
    <hr>
    DAO interface is [BlogDao]. 
    It has 4 methods:
    - `addEntry(BlogEntry blogEntry)`
    - `findAllEntries()`
    - `findEntriesBySlug(String slug)`
    - `removeEntry(BlogEntry newBlogEntry)`
    
    First three methods were given. `removeEntry` method I added in 
    order to remove entry on edit page and when old entry is edited,
    old one is removed, preserving the comments, and new one is added.
    Method `findEntriesBySlug` actually find entries by unique hashId
    generated for each blog entry from date and title, see 
    `setSlugUsingTitleAndCreationDate()` in [BlogEntry], 
    and implementation in [Sql2oBlogDao]. 
    
    Implementation of dao is called [Sql2oBlogDao]. 
<hr>
5.  <a id="task-5"></a> 
    Add necessary routes
    <hr>
    Following routes were added:
    - password page : `get("/password")` and `post("/password")`.
    - home page : `get("/")`.
    - entries detail page: `get("/entries/detail/:hashId/:slugFromTitle")`
    - post comments on entries detail page: 
        `post("/entries/detail/:hashId/:slugFromTitle")`
    - new entry page (password-protected): with `get("/entries/new")` 
        and `post("/entries/new")`
    - edit entry page (password-protected: 
    `get("/entries/edit/:hashId/:slugFromTitle")`
    - save entry on edit entry page: 
        `post("/entries/save/:hashId/:slugFromTitle")`
    - remove entry on edit page (made through get request) : 
        `get("/entries/remove/:hashId/:slugFromTitle")`
    - when [NotFoundException] is thrown, user is redirected to: 
        `exception(ApiError.class)` not-found page
<hr>
6.  <a id="task-6"></a>
    Create index view as the homepage. This view contains a list of 
    blog entries, which displays Title, Date/Time Created. Title should 
    be hyperlinked to the detail page for each blog entry. Include a 
    link to add an entry.
    <hr>
    Index page `get("/")` is modeled exactly as in mockup, also tags 
    are added after creation date. Three test entries displayed on the 
    page, as asked. Link to add an entry is bound to top right button. 
    Titles of entries will redirect to detailed entry pages. 
    Tags are hollow links right now. Later they will redirect to pages 
    sorted by tag. Top left Spark blog logo will lead to homepage.
    "Contact us" and "Terms" at the bottom of the page right now do not 
    lead anywhere.
<hr>
7. <a id="task-7"></a>
    Create detail page displaying blog entry and submitted comments. 
    Detail page should also display a comment form with Name and 
    Comment. Include a link to edit the entry.
    <hr>
    Detail page `get("/entries/detail/:hashId/:slugFromTitle")` displays
    title, comment, body of entry and tags. User can add comments, 
    however author field and entry field right now are required. From
    this page user can edit entry. Link is right before entry's body.
    Tags are shown, as hollow hyperlinks, for now. Links to add new
    entry, go home, "contact us" and "terms", are also here, just like 
    at home page
<hr>
8. <a id="task-8"></a>
    Create a password page that requires a user to provide 'admin' 
    as the username. This page should display before adding or 
    editing a comment if there is no cookie present that has the 
    value 'admin'.
    <hr>
    Password page is implemented in `get("/password")` with post
    request `post("/password")`. Filter is implemented in `before` 
    lambda to check if the cookie with name "password" and value 
    "admin" is set. The before filter is applied for:
    - edit entry page : `/entries/edit/*`
    - new entry page: `/entries/new`
    - save entry post request: `/entries/save/*`
    - remove entry get request: `/entries/remove/*`
    
    Note: In `before` filter address of protected page saved to session 
    attribute, so that later after right password, user will be 
    redirected to page where he was before.
<hr>
9.  <a id="task-9"></a>
    Create add/edit page that is blocked by a password page, 
    using before filter.
    <hr>
    See [task 8](#task-8).
<hr>
10. <a id="task-10"></a>
    Use CSS to style headings, font colors, blog entry container 
    colors, body colors.
    <hr>
    Provided CSS files are used, with couple of small additions, see
    [site.css].
<hr>
### Extra Credit
<hr>
11. <a id="task-11"></a>
    Add tags to blog entries, which enables the ability to categorize. 
    A blog entry can exist with no tags, or have multiple tags.
    <hr>
    Blog entries can have tags. On homepage and "detail" pages 
    tags are displayed with
    "#" symbols, and implemented as hyperlinks, so that in future it
    is possible to redirect user to page with list of entries, 
    containing tags. On "edit" and "new" pages there is a text field
    where tags can be entered. User input processed using 
    `slugifyTagsStringAndAddToTagsMember` function in [BlogEntry]. It
    is used in BlogEntry constructor. Tags can be any letters, numbers,
    hyphen and underscore. String with tags entered by user is
    processed by groups of these valid characters, and then gets 
    slugified, to be converted to valid link, so that we can later
    access entries with tags, like /entries/tags/{slug-from-tag}. This
    is not yet realized in current version.
<hr>
12. <a id="task-12"></a>
    Add the ability to delete a blog entry.
    <hr>
    Entry can be removed from edit page, using 
    `get("/entries/remove/")` request. It is a get request, because we
    don't need any information from user. I added remove button, changed
    styling a bit, for it to fit. It is password protected action, so 
    that if we don't have cookie with name "password" and name "admin",
    this action is prohibited.
<hr>
13. <a id="task-13"></a>
    Issue a cookie upon entering the password, and check for it upon 
    adding or editing a post.
    <hr>
    See [Task 8](#task-8)
    
