## Techdegree project 4
### Blog with Spark
<hr>
### Table of Contents
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
<hr>

[resources]:src/main/resources 
[templates]:src/main/resources/templates
[css]:src/main/resources/public/css 
[BlogEntry]:src/main/java/com/teamtreehouse/blog/model/BlogEntry.java 
[Comment]:src/main/java/com/teamtreehouse/blog/model/Comment.java 
[Date]:src/main/java/com/teamtreehouse/blog/model/Date.java 
[BlogDao]:src/main/java/com/teamtreehouse/blog/dao/BlogDao.java
[SimpleBlogEntryDAO]:src/main/java/com/teamtreehouse/blog/dao/SimpleBlogEntryDAO.java 
[NotFoundException]:src/main/java/com/teamtreehouse/blog/exception/NotFoundException.java 

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
    and implementation in [SimpleBlogEntryDAO]. 
    
    Implementation of dao is called [SimpleBlogEntryDAO]. 
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