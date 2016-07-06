package com.teamtreehouse.blog;

import com.teamtreehouse.blog.dao.Sql2oBlogDao;
import com.teamtreehouse.blog.dao.Sql2oEntryDao;
import com.teamtreehouse.blog.exception.ApiError;
import com.teamtreehouse.blog.exception.NotFoundException;
import com.teamtreehouse.blog.model.BlogEntry;
import org.sql2o.Sql2o;
import spark.Filter;
import spark.ModelAndView;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import static spark.Spark.*;

public class Main {
    private static Sql2oBlogDao sSql2oBlogDao;
    protected static Sql2oBlogDao getSimpleBlogDao() {
        return sSql2oBlogDao;
    }
    private static Sql2oEntryDao sSql2OEntryDao;
    protected static Sql2oEntryDao getSimpleEntryDao() {
        return sSql2OEntryDao;
    }

    private static String sSessionId;
    protected static String getSessionId() {
        return sSessionId;
    }

//    private static BlogEntry createTestBlogEntryWithComments(
//            String blogTitle,
//            String blogBody,
//            String stringWithTags) {
//        BlogEntry testBlogEntry = new BlogEntry(blogTitle, blogBody);
//        testBlogEntry.slugifyTagsStringAndAddToTagsMember(stringWithTags);
//        return testBlogEntry;
//    }
//    private static void fillDaoWithThreeTestEntries() {
//        sSql2oBlogDao.addEntry(createTestBlogEntryWithComments(
//               "Title1", "Body1", "tag1 tag2"
//        ));
//        sSql2OEntryDao.addComment(
//                new Comment(sSql2oBlogDao.findEntryByHashId())
//        )
//        sSql2oBlogDao.addEntry(createTestBlogEntryWithComments(
//                "Title2", "Body2", "Comment2", new Date(2L), "Author2", "tag2"
//        ));
//        sSql2oBlogDao.addEntry(createTestBlogEntryWithComments(
//                "Title3", "Body3", "Comment3", new Date(3L), "Author3", "tag3"
//        ));
//    }

    public static void main(String[] args) {
        String dataSource = "jdbc:h2:~/spark-blog.db";
        // used in testing of Api
        if (args.length > 0 ) {
            if (args.length != 2) {
                System.out.println("java Api <port> <dataSource>");
                System.exit(1);
            }
            // no checks here for args, we run this with args in testing
            port(Integer.parseInt(args[0]));
            dataSource = args[1];
        }
        staticFileLocation("/public");
        // our master password, the worst security ever :)
        String masterPassword = "admin";
        // Not found message
        String notFoundMessage = "No such entry found";
        // I also use external static dao for testing, it is not the best way
        // I know, but in the absence of database I see no other way
        String connectionString = String.format(
                "%s;INIT=RUNSCRIPT from 'classpath:db/init.sql'",
                dataSource);
        Sql2o sql2o = new Sql2o(connectionString, "", "");
        sSql2oBlogDao = new Sql2oBlogDao(sql2o);
        sSql2OEntryDao = new Sql2oEntryDao();
//        sSql2OEntryDao = new Sql2oEntryDao(sSql2oBlogDao);
//        fillDaoWithThreeTestEntries();
        Sql2oBlogDao sql2oBlogDao = sSql2oBlogDao;
        // test dao setup
        // redirect user to password page if cookie password is null, or
        // set to anything other than master password. Session attribute is
        // is set to remember page we were previously, so that if password is
        // successful, we get back where we were
        String[] protectedRoutes =
                new String[] {
                        "/entries/new",
                        "/entries/edit/*",
                        "/entries/remove/*",
                        "/entries/save/*"};
        Filter filter = (request, response) -> {
            request.session().attribute("protected-page",request.uri());
            sSessionId = request.session().id();
            if (request.cookie("password") == null
                    || !request.cookie("password").equals(masterPassword)) {
               response.redirect("/password");
               // stop from processing with initial request
               halt();
            }
        };
        for (String route: protectedRoutes) {
            before(route,filter);
        }

        // password page, get and post
        get("/password",(request, response) -> {
            return new ModelAndView(null, "password.hbs");
        }, new HandlebarsTemplateEngine());
        // password page
        post("/password",(request, response) -> {
            // take password from input of form
            String password = request.queryParams("password");
            // put password in cookie
            response.cookie("password",password);
            // if session is new, we redirect back to home page, may be not the
            // best solution, but not 500 internal error, else redirect to
            // page from where password-required action was made, see filters
            if (request.session().isNew()) {
                response.redirect("/");
            } else {
                response.redirect(request.session().attribute("protected-page"));
            }
            return null;
        });


        // main page with blog entries
        get("/",(request, response) -> {
            Map<String,Object> model = new HashMap<>();
            model.put("entries", sql2oBlogDao.findAllEntries());
            return new ModelAndView(model, "index.hbs");
        }, new HandlebarsTemplateEngine());

        // entry detail page, get and post: see below, for comment
        // ApiError is thrown when entry is not found by slug
        get("/entries/detail/:hashId/:slugFromTitle",(request, response) -> {
            String hashId = request.params("hashId");
            // put entry and comments in detail page
            Map<String, Object> model = new HashMap<>();
            // check for blog entry existence
            BlogEntry blogEntry;
            try {
                blogEntry =
                        sql2oBlogDao.findEntryByHashId(hashId);
            } catch (NotFoundException nfe) {
                throw new ApiError(404, notFoundMessage);
            }
            model.put("entry", blogEntry);
//            model.put("comments", blogEntry.getComments());
//            model.put("tags", blogEntry.getTags());
            return new ModelAndView(model, "detail.hbs");
        }, new HandlebarsTemplateEngine());
        // create new comment on entries detail page
        // ApiError is thrown when entry is not found by slug
        post("/entries/detail/:hashId/:slugFromTitle", (request, response) -> {
            // get old blog entry by slug
            String hashId = request.params("hashId");
            String slugFromTitle = request.params("slugFromTitle");
            // try to find blog entry
            BlogEntry blogEntry;
            try {
                blogEntry = sql2oBlogDao.findEntryByHashId(hashId);
            } catch (NotFoundException notFoundException) {
                throw new ApiError(404, notFoundMessage);
            }
            // create new comment title(non-null, see new.hbs) and body
            String authorName = request.queryParams("name");
            String body = request.queryParams("body");
            if (authorName.isEmpty()) {
                authorName = "Anonymous";
            }
//            Comment comment = new Comment(body, authorName);
            // no check here because its hard to make comments same, unless
            // they are done in the same second, which is impossible in real
            // we scenario, so just add comment, no check
//            blogEntry.addComment(comment);
            // redirect back to entry detail page
            response.redirect("/entries/detail/" + hashId +
                    "/" + slugFromTitle);
            return null;
        });

        // new entry page, with get in all pages, and post
        get("/entries/new",(request, response) -> {
            return new ModelAndView(null,"new.hbs");
        }, new HandlebarsTemplateEngine());
        // create new entry
        post("/entries/new",(request, response) -> {
            // create new blog entry with title(non-null, see new.hbs) and body
            String newTitle = request.queryParams("title");
            String newBody = request.queryParams("body");
            BlogEntry newBlogEntry = new BlogEntry(newTitle, newBody);
            // because our entries are unique (equals includes Date), no checks
            // here
            sql2oBlogDao.addEntry(newBlogEntry);
            response.status(201);
            response.redirect("/");
            return null;
        });

        // entry edit page
        // ApiError is thrown when entry is not found by slug
        get("/entries/edit/:hashId/:slugFromTitle",(request, response) -> {
            // try to find entry by slug
            String hashId = request.params("hashId");
            BlogEntry blogEntry;
            try {
                blogEntry =
                        sql2oBlogDao.findEntryByHashId(hashId);
            } catch (NotFoundException nfe) {
                throw new ApiError(404, notFoundMessage);
            }
            // put found entry to model
            Map<String,Object> model = new HashMap<>();
            model.put("entry", blogEntry);
            // return model and view
            return new ModelAndView(model, "edit.hbs");
        }, new HandlebarsTemplateEngine());


        // save entry post in edit.hbs
        // ApiError is thrown when entry is not found by slug
        post("/entries/save/:hashId/:slugFromTitle", (request, response) -> {
            String hashId = request.params("hashId");
            // get old blog entry by slug
            BlogEntry oldBlogEntry;
            try {
                oldBlogEntry =
                        sql2oBlogDao.findEntryByHashId(hashId);
            } catch (NotFoundException nfe) {
                throw new ApiError(404, notFoundMessage);
            }
            // create new blog entry with title(non-null, see new.hbs) and body
            String newTitle = request.queryParams("title");
            String newBody = request.queryParams("body");
            String newTags = request.queryParams("tags");
            BlogEntry newBlogEntry = new BlogEntry(newTitle, newTags);
            // even if user didn't change anything, because he pushed edit,
            // entry will have new creation date, the simplest way was, as I
            // thought is to remove and add new entry to DAO
            sql2oBlogDao.removeEntry(oldBlogEntry);
            sql2oBlogDao.addEntry(newBlogEntry);
            // save new title and entry
            response.redirect("/");
            return null;
        });

        // remove entry from detail page
        get("/entries/remove/:hashId/:slugFromTitle", (request, response) -> {
            String hashId = request.params("hashId");
            // get old blog entry by slug
            BlogEntry blogEntry;
            try {
                blogEntry = sql2oBlogDao.findEntryByHashId(hashId);
            } catch (NotFoundException nfe) {
                throw new ApiError(404, notFoundMessage);
            }
            // remove entry from dao
            sql2oBlogDao.removeEntry(blogEntry);
            // redirect to home page
            response.redirect("/");
            return null;
        });

        // all exceptions will be processed through this lambda
        exception(ApiError.class, (exception, request, response) -> {
            ApiError apiError = (ApiError) exception;
            Map<String, Object> model = new HashMap<>();
            model.put("status", apiError.getStatus());
            model.put("errorMessage", apiError.getMessage());
            response.status(apiError.getStatus());
            HandlebarsTemplateEngine handlebarsTemplateEngine =
                    new HandlebarsTemplateEngine();
            String html = handlebarsTemplateEngine.render(
                    new ModelAndView(model, "not-found.hbs"));
            response.body(html);
        });
    }
}
