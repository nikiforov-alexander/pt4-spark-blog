## Techdegree project 4
### Blog with Spark
<hr>
### Table of Contents
* [1.] (#task-1) Use supplied mockup files to build personal blog.
* [2.] (#task-2) In IntelliJ IDEA, create a Gradle project. Add all 
        required Spark dependencies, and create the directory and package 
        structure of the application. Save all static assets into the 
        proper directory.
* [3.] (#task-3) Create model classes for blog entries and blog comments.

<hr>
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
    directory is created and marked as [resources](src/main/resources). 
    CSS files are in 
    [resources/public/css](src/main/resources/public/css). 
    Template .hbs files are in 
    [resources/templates](src/main/resources/templates)
<hr>
3.  <a id="task-3"></a>
    Create model classes for blog entries and blog comments. Model 
    classes are:
    - [BlogEntry](src/main/java/com/teamtreehouse/blog/model/BlogEntry.java) 
    - [Comment](src/main/java/com/teamtreehouse/blog/model/Comment.java) 
    
    I also added [Date](src/main/java/com/teamtreehouse/blog/model/Date.java) 
    class, just for convenience, that inherits 
    `java.util.Date` class, but has two additional methods, that return
    date in right machine-readable format for html datetime element: 
    `getHtmlCreationDate()` 
    and return date in format provided in mockup files for users to see
    `getCreationDateString()`
<hr>
