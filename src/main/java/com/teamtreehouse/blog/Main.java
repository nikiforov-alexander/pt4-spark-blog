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
import java.util.stream.Stream;

public class Main {
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
        // dao
        SimpleBlogEntryDAO simpleBlogEntryDAO =
                new SimpleBlogEntryDAO();
        // test dao setup
        BlogEntry testBlogEntry = new BlogEntry("Title","Test body");
        simpleBlogEntryDAO.addEntry(testBlogEntry);
        Comment comment1 = new Comment("Comment1", new Date(1L), "John Doe");
        Comment comment2 = new Comment("Comment2", new Date(2L), "John Doe");
        testBlogEntry.addComment(comment1);
        testBlogEntry.addComment(comment2);
        // redirect user to password page if cookie password is null, or
        // set to anything other than master password. Session attribute is
        // is set to remember page we were previously, so that if password is
        // successful, we get back where we were
        String[] protectedRoutes =
                new String[] {"/entries/new", "/entries/edit/*"};
        Filter filter = (request, response) -> {
            request.session().attribute("protected-page",request.uri());
            if (request.cookie("password") == null
                    || !request.cookie("password").equals(masterPassword)) {
               response.redirect("/password");
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
            response.redirect(request.session().attribute("protected-page"));
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
        get("/entries/detail/:slug",(request, response) -> {
            String slug = request.params("slug");
            // put entry and comments in detail page
            Map<String, Object> model = new HashMap<>();
            // check for blog entry existence
            BlogEntry blogEntry;
            try {
                blogEntry =
                        simpleBlogEntryDAO.findEntryBySlug(slug);
            } catch (NotFoundException nfe) {
                throw new ApiError(404, "No such entry found");
            }
            model.put("entry", blogEntry);
            model.put("comments", blogEntry.getComments());
            return new ModelAndView(model, "detail.hbs");
        }, new HandlebarsTemplateEngine());
        // create new comment on entries detail page
        // ApiError is thrown when entry is not found by slug
        post("/entries/detail/:slug", (request, response) -> {
            // get old blog entry by slug
            String slug = request.params("slug");
            // try to find blog entry
            BlogEntry blogEntry;
            try {
                blogEntry = simpleBlogEntryDAO.findEntryBySlug(slug);
            } catch (NotFoundException notFoundException) {
                throw new ApiError(404, "No such entry found");
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
            response.redirect("/entries/detail/" + slug);
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
            simpleBlogEntryDAO.addEntry(newBlogEntry);
            response.status(201);
            response.redirect("/");
            return null;
        });

        // entry edit page
        // ApiError is thrown when entry is not found by slug
        get("/entries/edit/:slug",(request, response) -> {
            // try to find entry by slug
            String slug = request.params("slug");
            BlogEntry blogEntry;
            try {
                blogEntry =
                        simpleBlogEntryDAO.findEntryBySlug(slug);
            } catch (NotFoundException nfe) {
                throw new ApiError(404, "No such entry found");
            }
            // put found entry to model
            Map<String,Object> model = new HashMap<>();
            model.put("entry", blogEntry);
            // return model and view
            return new ModelAndView(model, "edit.hbs");
        }, new HandlebarsTemplateEngine());


        // save entry post in edit.hbs
        // ApiError is thrown when entry is not found by slug
        post("/entries/save/:slug", (request, response) -> {
            // get old blog entry by slug
            String slug = request.params("slug");
            BlogEntry oldBlogEntry;
            try {
                oldBlogEntry =
                        simpleBlogEntryDAO.findEntryBySlug(slug);
            } catch (NotFoundException nfe) {
                throw new ApiError(404, "No such entry found");
            }
            // create new blog entry with title(non-null, see new.hbs) and body
            String newTitle = request.queryParams("title");
            String newBody = request.queryParams("body");
            BlogEntry newBlogEntry = new BlogEntry(newTitle,
                    newBody,
                    oldBlogEntry.getComments());
            // even if user didn't change anything, because he pushed edit,
            // entry will have new creation date, the simplest way was, as I
            // thought is to remove and add new entry to DAO
            simpleBlogEntryDAO.removeEntry(oldBlogEntry);
            simpleBlogEntryDAO.addEntry(newBlogEntry);
            // save new title and entry
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
