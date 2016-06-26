package com.teamtreehouse.blog;

import static spark.Spark.*;

import com.teamtreehouse.blog.dao.SimpleBlogEntryDAO;
import com.teamtreehouse.blog.model.BlogEntry;
import com.teamtreehouse.blog.model.Comment;
import com.teamtreehouse.blog.model.Date;
import spark.ModelAndView;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        // used in testing of Api
        if (args.length > 0 ) {
            if (args.length != 2) {
                System.out.println("java Api <port> <dataSource>");
                System.exit(1);
            }
            // no checks here for args, we run this with args in testing
            port(Integer.parseInt(args[0]));
        }
        staticFileLocation("/public");
        String masterPassword = "admin";
        SimpleBlogEntryDAO simpleBlogEntryDAO =
                new SimpleBlogEntryDAO();
        BlogEntry testBlogEntry = new BlogEntry("Title","Test body");
        simpleBlogEntryDAO.addEntry(testBlogEntry);
        Comment comment1 = new Comment("Comment1", new Date(1L), "John Doe");
        Comment comment2 = new Comment("Comment2", new Date(2L), "John Doe");
        testBlogEntry.addComment(comment1);
        testBlogEntry.addComment(comment2);
        // redirect user to password page if cookie password is null, or
        // set to anything other than master password
        before((request, response) -> {
            if (request.cookie("password") == null) {
                response.redirect("/password");
                halt();
            } else {
                if (!request.cookie("password").equals(masterPassword)) {
                    response.redirect("/password");
                    halt();
                }
            }
        });

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
            response.redirect("/");
            return null;
        });


        // main page with blog entries
        get("/",(request, response) -> {
            Map<String,Object> model = new HashMap<>();
            model.put("entries",simpleBlogEntryDAO.findAllEntries());
            return new ModelAndView(model, "index.hbs");
        }, new HandlebarsTemplateEngine());

        // entry detail page, get and post: see below, for comment
        get("/entries/detail/:slug",(request, response) -> {
            String blogEntrySlug = request.params("slug");
                // put entry and comments in detail page
                Map<String, Object> model = new HashMap<>();
                BlogEntry blogEntry =
                        simpleBlogEntryDAO.findEntryBySlug(blogEntrySlug);
                model.put("entry", blogEntry);
                model.put("comments", blogEntry.getComments());
                return new ModelAndView(model, "detail.hbs");
        }, new HandlebarsTemplateEngine());
        // create new comment on entries detail page
        post("/entries/detail/:slug", (request, response) -> {
            // get old blog entry by slug
            String slug = request.params("slug");
            BlogEntry blogEntry = simpleBlogEntryDAO.findEntryBySlug(slug);
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
            // if dao contains entry we replace old one with new
            if (!simpleBlogEntryDAO.containsEntry(newBlogEntry)) {
                simpleBlogEntryDAO.addEntry(newBlogEntry);
            } else {
                // TODO: set flash message, that it is impossible to create
                // TODO: test what if new blog entry is already in dao
                System.out.println("equal");
            }
            // save new title and entry
            response.redirect("/");
            return null;
        });

        // entry edit page
        get("/entries/edit/:slug",(request, response) -> {
            String blogEntrySlug = request.params("slug");
            Map<String,Object> model = new HashMap<>();
            BlogEntry blogEntry =
                    simpleBlogEntryDAO.findEntryBySlug(blogEntrySlug);
            model.put("entry", blogEntry);
            return new ModelAndView(model, "edit.hbs");
        }, new HandlebarsTemplateEngine());


        // save entry post in edit.hbs
        post("/entries/save/:slug", (request, response) -> {
            // get old blog entry by slug
            String slug = request.params("slug");
            BlogEntry oldBlogEntry = simpleBlogEntryDAO.findEntryBySlug(slug);
            // create new blog entry with title(non-null, see new.hbs) and body
            String newTitle = request.queryParams("title");
            String newBody = request.queryParams("body");
            BlogEntry newBlogEntry = new BlogEntry(newTitle,
                    newBody,
                    oldBlogEntry.getComments());
            // if dao contains entry we replace old one with new
            if (!simpleBlogEntryDAO.containsEntry(newBlogEntry)) {
                simpleBlogEntryDAO.removeEntry(oldBlogEntry);
                simpleBlogEntryDAO.addEntry(newBlogEntry);
            } else {
                // TODO: set flash message, that it is impossible to create
                // TODO: test what if new blog entry is already in dao
                System.out.println("equal");
            }
            // save new title and entry
            response.redirect("/");
            return null;
        });

    }
}
