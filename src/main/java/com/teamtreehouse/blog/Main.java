package com.teamtreehouse.blog;

import com.teamtreehouse.blog.dao.Sql2oBlogDao;
import com.teamtreehouse.blog.dao.Sql2oEntryDao;
import com.teamtreehouse.blog.exception.ApiError;
import com.teamtreehouse.blog.exception.DaoException;
import com.teamtreehouse.blog.model.BlogEntry;
import com.teamtreehouse.blog.model.Comment;
import org.sql2o.Sql2o;
import spark.Filter;
import spark.ModelAndView;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static spark.Spark.*;

public class Main {
    private static Sql2oBlogDao sSql2oBlogDao;
    protected static Sql2oBlogDao getSql2oBlogDao() {
        return sSql2oBlogDao;
    }
    private static Sql2oEntryDao sSql2oEntryDao;
    protected static Sql2oEntryDao getSql2oEntryDao() {
        return sSql2oEntryDao;
    }

    private static void fillDaosWithTestEntriesAndComments() throws DaoException {
        BlogEntry blogEntry1 = new BlogEntry("Title1", "Body1", new Date(1L));
        BlogEntry blogEntry2 = new BlogEntry("Title2", "Body2", new Date(2L));
        BlogEntry blogEntry3 = new BlogEntry("Title3", "Body3", new Date(3L));
        sSql2oBlogDao.addEntry(blogEntry1);
        sSql2oBlogDao.addEntry(blogEntry2);
        sSql2oBlogDao.addEntry(blogEntry3);
        Comment comment1 =
                new Comment(blogEntry1.getId(), "Body1", new Date(1L), "Name1");
        Comment comment2 =
                new Comment(blogEntry2.getId(), "Body2", new Date(2L), "Name2");
        Comment comment3 =
                new Comment(blogEntry3.getId(), "Body3", new Date(3L), "Name3");
        sSql2oEntryDao.addComment(comment1);
        sSql2oEntryDao.addComment(comment2);
        sSql2oEntryDao.addComment(comment3);
    }

    public static void main(String[] args) {
        String dataSource = "jdbc:h2:./spark-blog";
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
        sSql2oEntryDao = new Sql2oEntryDao(sql2o);
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
            if (request.cookie("password") == null
                    || !request.cookie("password").equals(masterPassword)) {
                // set unauthorized status
                response.status(401);
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
            // fill blog with three test entries only if blog is empty
            if (sSql2oBlogDao.findAllEntries().isEmpty()) {
               try {
                   fillDaosWithTestEntriesAndComments();
               } catch (DaoException daoException) {
                   System.out.println("Could not fill DAO with entries");
               }
            }
            Map<String,Object> model = new HashMap<>();
            model.put("entries", sSql2oBlogDao.findAllEntries());
            return new ModelAndView(model, "index.hbs");
        }, new HandlebarsTemplateEngine());

        // entry detail page, get and post: see below, for comment
        // ApiError is thrown when entry is not found
        get("/entries/detail/:id/:slugFromTitle",(request, response) -> {
            // redirect to not found page if user types
            // /entries/detail/a/some-slug
            int entryId;
            try {
                entryId = Integer.parseInt(request.params("id"));
            } catch (NumberFormatException nfe) {
                throw new ApiError(404, notFoundMessage);
            }
            // put entry and comments in detail page
            Map<String, Object> model = new HashMap<>();
            // check for blog entry existence
            BlogEntry blogEntry = sSql2oBlogDao.findEntryById(entryId);
            if (blogEntry == null) {
                throw new ApiError(404, notFoundMessage);
            }
            model.put("entry", blogEntry);
            // can be null
            model.put("comments", sSql2oEntryDao.findByEntryId(entryId));
            return new ModelAndView(model, "detail.hbs");
        }, new HandlebarsTemplateEngine());
        // create new comment on entries detail page
        // ApiError is thrown when entry is not found by slug
        post("/entries/detail/:id/:slugFromTitle", (request, response) -> {
            // redirect to not found page if user types
            // /entries/detail/a/some-slug
            int entryId;
            try {
                entryId = Integer.parseInt(request.params("id"));
            } catch (NumberFormatException nfe) {
                throw new ApiError(404, notFoundMessage);
            }
            // try to find blog entry
            BlogEntry blogEntry = sSql2oBlogDao.findEntryById(entryId);
            // redirect to not found page if null
            if (blogEntry == null) {
                throw new ApiError(404, notFoundMessage);
            }
            // create new comment title (non-null, see new.hbs) and body
            String authorName = request.queryParams("name");
            String body = request.queryParams("body");
            // author name is set to "Anonymous" is empty
            if (authorName.isEmpty()) {
                authorName = "Anonymous";
            }
            Comment comment = new Comment(entryId, body, authorName);
            // add comment
            try {
                sSql2oEntryDao.addComment(comment);
            } catch (DaoException e) {
                System.out.println(e.getMessage());
            }
            // redirect back to entry detail page
            String slugFromTitle = request.params("slugFromTitle");
            response.redirect("/entries/detail/" + entryId +
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
            // add entry to DAO
            try {
                sSql2oBlogDao.addEntry(newBlogEntry);
                // setting created status
                response.status(201);
            } catch (DaoException e) {
                System.out.println(e.getMessage());
            }
            // redirecting back home
            response.redirect("/");
            return null;
        });

        // entry edit page
        // ApiError is thrown when entry is not found by id
        get("/entries/edit/:id/:slugFromTitle",(request, response) -> {
            // redirect to not found page if user types
            // /entries/edit/a/some-slug
            int entryId;
            try {
                entryId = Integer.parseInt(request.params("id"));
            } catch (NumberFormatException nfe) {
                throw new ApiError(404, notFoundMessage);
            }
            // try to find entry by id
            BlogEntry blogEntry =
                        sSql2oBlogDao.findEntryById(entryId);
            // redirect to not found page if null
            if (blogEntry == null) {
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
        post("/entries/save/:id/:slugFromTitle", (request, response) -> {
            // redirect to not found page if user types
            // non-integer id
            int entryId;
            try {
                entryId = Integer.parseInt(request.params("id"));
            } catch (NumberFormatException nfe) {
                throw new ApiError(404, notFoundMessage);
            }
            // try to get old blog entry by slug, if not, return 404
            BlogEntry oldBlogEntry =
                        sSql2oBlogDao.findEntryById(entryId);
            if (oldBlogEntry == null) {
                throw new ApiError(404, notFoundMessage);
            }
            // create new blog entry with title(non-null, see new.hbs) and body
            String newTitle = request.queryParams("title");
            String newBody = request.queryParams("body");
            BlogEntry newBlogEntry = new BlogEntry(newTitle, newBody);
            // even if user didn't change anything, because he pushed edit,
            // entry will have new creation date, the simplest way was, as I
            // thought is to remove and add new entry to DAO
            // save old comments
            List<Comment> listOfAssociatedComments =
                    sSql2oEntryDao.findByEntryId(entryId);
            try {
                sSql2oBlogDao.removeEntryById(entryId);
            } catch (DaoException daoException) {
                // print message to us, not for user, and redirect back home
                System.out.println(daoException.getMessage());
                response.redirect("/");
                return null;
            }
            try {
                // add new entry, get his new id
                int newEntryId = sSql2oBlogDao.addEntry(newBlogEntry);
                // add comments to database with this id, cloning involved
                for (Comment comment : listOfAssociatedComments) {
                    Comment commentClone =
                            new Comment(
                                    newEntryId,
                                    comment.getBody(),
                                    comment.getDate(),
                                    comment.getAuthor());
                    sSql2oEntryDao.addComment(commentClone);
                }
            } catch (DaoException daoException) {
                System.out.println(daoException.getMessage());
            }
            // redirect back home
            response.redirect("/");
            return null;
        });

        // remove entry from detail page
        get("/entries/remove/:id/:slugFromTitle", (request, response) -> {
            // redirect to not found page if user types
            // /entries/remove/a/some-slug
            int entryId;
            try {
                entryId = Integer.parseInt(request.params("id"));
            } catch (NumberFormatException nfe) {
                throw new ApiError(404, notFoundMessage);
            }
            // get blog entry by id, or throw not found error page
            BlogEntry blogEntry = sSql2oBlogDao.findEntryById(entryId);
            if (blogEntry == null) {
                throw new ApiError(404, notFoundMessage);
            }
            // remove entry from dao
            try {
                sSql2oBlogDao.removeEntryById(entryId);
            } catch (DaoException daoException) {
                // if there is error, on server we'll see problem, but user
                // should not now about it
                System.out.println(daoException.getMessage());
            }
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
