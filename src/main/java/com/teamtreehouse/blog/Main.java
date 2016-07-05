package com.teamtreehouse.blog;

import static spark.Spark.*;

import com.teamtreehouse.blog.dao.SimpleBlogEntryDAO;
import com.teamtreehouse.blog.exception.ApiError;
import com.teamtreehouse.blog.exception.NotFoundException;
import com.teamtreehouse.blog.model.BlogEntry;
import com.teamtreehouse.blog.model.Comment;
import com.teamtreehouse.blog.model.Date;
import spark.Filter;
import spark.ModelAndView;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.util.HashMap;
import java.util.Map;

public class Main {
    protected static SimpleBlogEntryDAO mSimpleBlogEntryDAO;
    public static String mSessionId;

    private static BlogEntry createTestBlogEntryWithComments(
            String blogTitle,
            String blogBody,
            String CommentName,
            Date CommentDate,
            String CommentAuthor,
            String stringWithTags) {
        BlogEntry testBlogEntry = new BlogEntry(blogTitle, blogBody);
        Comment comment = new Comment(CommentName, CommentDate, CommentAuthor);
        testBlogEntry.addComment(comment);
        testBlogEntry.slugifyTagsStringAndAddToTagsMember(stringWithTags);
        return testBlogEntry;
    }
    private static void fillDaoWithThreeTestEntries() {
        mSimpleBlogEntryDAO.addEntry(createTestBlogEntryWithComments(
               "Title1", "Body1", "Comment1", new Date(1L), "Author1", "tag1 tag2"
        ));
        mSimpleBlogEntryDAO.addEntry(createTestBlogEntryWithComments(
                "Title2", "Body2", "Comment2", new Date(2L), "Author2", "tag2"
        ));
        mSimpleBlogEntryDAO.addEntry(createTestBlogEntryWithComments(
                "Title3", "Body3", "Comment3", new Date(3L), "Author3", "tag3"
        ));
    }

    public static void main(String[] args) {
        // used in testing of Api
        if (args.length > 0 ) {
            if (args.length != 1) {
                System.out.println("java Api <port>");
                System.exit(1);
            }
            // no checks here for args, we run this with args in testing
            port(Integer.parseInt(args[0]));
        }
        staticFileLocation("/public");
        // our master password, the worst security ever :)
        String masterPassword = "admin";
        // Not found message
        String notFoundMessage = "No such entry found";
        // I also use external static dao for testing, it is not the best way
        // I know, but in the absence of database I see no other way
        mSimpleBlogEntryDAO = new SimpleBlogEntryDAO();
        fillDaoWithThreeTestEntries();
        SimpleBlogEntryDAO simpleBlogEntryDAO = mSimpleBlogEntryDAO;
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
            mSessionId = request.session().id();
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
            model.put("entries",simpleBlogEntryDAO.findAllEntries());
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
                        simpleBlogEntryDAO.findEntryBySlug(hashId);
            } catch (NotFoundException nfe) {
                throw new ApiError(404, notFoundMessage);
            }
            model.put("entry", blogEntry);
            model.put("comments", blogEntry.getComments());
            model.put("tags", blogEntry.getTags());
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
                blogEntry = simpleBlogEntryDAO.findEntryBySlug(hashId);
            } catch (NotFoundException notFoundException) {
                throw new ApiError(404, notFoundMessage);
            }
            // create new comment title(non-null, see new.hbs) and body
            String authorName = request.queryParams("name");
            String body = request.queryParams("body");
            Comment comment = new Comment(body, authorName);
            // no check here because its hard to make comments same, unless
            // they are done in the same second, which is impossible in real
            // we scenario, so just add comment, no check
            blogEntry.addComment(comment);
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
            String newTags = request.queryParams("tags");
            BlogEntry newBlogEntry = new BlogEntry(newTitle, newBody, newTags);
            // because our entries are unique (equals includes Date), no checks
            // here
            simpleBlogEntryDAO.addEntry(newBlogEntry);
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
                        simpleBlogEntryDAO.findEntryBySlug(hashId);
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
                        simpleBlogEntryDAO.findEntryBySlug(hashId);
            } catch (NotFoundException nfe) {
                throw new ApiError(404, notFoundMessage);
            }
            // create new blog entry with title(non-null, see new.hbs) and body
            String newTitle = request.queryParams("title");
            String newBody = request.queryParams("body");
            String newTags = request.queryParams("tags");
            BlogEntry newBlogEntry = new BlogEntry(newTitle,
                    newBody,
                    oldBlogEntry.getComments(),
                    newTags);
            // even if user didn't change anything, because he pushed edit,
            // entry will have new creation date, the simplest way was, as I
            // thought is to remove and add new entry to DAO
            simpleBlogEntryDAO.removeEntry(oldBlogEntry);
            simpleBlogEntryDAO.addEntry(newBlogEntry);
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
                blogEntry = simpleBlogEntryDAO.findEntryBySlug(hashId);
            } catch (NotFoundException nfe) {
                throw new ApiError(404, notFoundMessage);
            }
            // remove entry from dao
            simpleBlogEntryDAO.removeEntry(blogEntry);
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
