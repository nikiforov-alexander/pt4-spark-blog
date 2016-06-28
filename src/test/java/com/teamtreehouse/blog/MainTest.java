package com.teamtreehouse.blog;

import com.teamtreehouse.blog.dao.SimpleBlogEntryDAO;
import com.teamtreehouse.blog.testing.ApiClient;
import com.teamtreehouse.blog.testing.ApiResponse;
import org.junit.*;
import spark.ModelAndView;
import spark.Spark;
import spark.template.handlebars.HandlebarsTemplateEngine;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class MainTest {
    public static final String PORT = "4568";
    private static final String notFoundMessage = "No such entry found";
    private ApiClient mApiClient;
    private SimpleBlogEntryDAO mSimpleBlogEntryDAO;
    private static final String mCookieWithPassword = "password=admin";

    @BeforeClass
    public static void startServer() {
        String[] args = {PORT};
        Main.main(args);
    }
    @AfterClass
    public static void stopServer() {
        Spark.stop();
    }

    @Before
    public void setUp() throws Exception {
        mSimpleBlogEntryDAO = new SimpleBlogEntryDAO();
        mApiClient = new ApiClient("http://localhost:" + PORT);
    }

    @Test
    public void emptyDaoPageIsTheSameAsModeled() throws Exception {
//        BlogEntry blogEntry = new BlogEntry("title", "body");
//        mSimpleBlogEntryDAO.addEntry(blogEntry);
//        Map<String, Object> model = new HashMap<>();
//        model.put("entries",mSimpleBlogEntryDAO.findAllEntries());
        HandlebarsTemplateEngine handlebarsTemplateEngine =
                new HandlebarsTemplateEngine();
        String indexHtml = handlebarsTemplateEngine
                .render(new ModelAndView(null, "index.hbs"));
//        Document document = Jsoup.parse(indexHtml);
//        Elements links = document
//                .getElementsByAttributeValueStarting("href", "/entries/detail");
//        String titleOfNewEntry = links.text();
        ApiResponse apiResponse =
                mApiClient.request("GET", "/");
        assertEquals(indexHtml, apiResponse.getBody());
    }

    private String getHtmlOfPageWithHbsWithNullModel(String hbsFileName) {
        HandlebarsTemplateEngine handlebarsTemplateEngine =
                new HandlebarsTemplateEngine();
        return handlebarsTemplateEngine
                .render(new ModelAndView(null, hbsFileName));
    }
    private String getHtmlOfUnAuthorisedRequestToPage(String pageUri) {
        ApiResponse apiResponse =
                mApiClient.request("GET", pageUri);
        return apiResponse.getBody();
    }
    private String getHtmlOfAuthorisedRequestToPage(String pageUri) {
        ApiResponse apiResponse =
                mApiClient.request("GET", pageUri, null, mCookieWithPassword);
        return apiResponse.getBody();
    }
    private String getHtmlOfPageWithHbsWithModel(String hbsFileName,
                                                 Map<?,?> model) {
        HandlebarsTemplateEngine handlebarsTemplateEngine =
                new HandlebarsTemplateEngine();
        return handlebarsTemplateEngine
                .render(new ModelAndView(model, hbsFileName));
    }
//    throw new ApiError(404, "No such entry found");

    @Test
    public void unauthorisedRequestOnNewEntryPageRedirectsToPasswordPage()
        throws Exception {
        // Given no cookies with password
        // When get to new entries page
        // password html page should come as a response
        assertEquals(
                getHtmlOfPageWithHbsWithNullModel("password.hbs"),
                getHtmlOfUnAuthorisedRequestToPage("/entries/new"));
    }
    @Test
    public void unauthorisedRequestOnEditEntryPageRedirectsToPasswordPage()
            throws Exception {
        // Given no cookies with password
        // When get request to edit entries page is made
        // Then password html page should come as a response, even if there are
        // no such entry
        assertEquals(
                getHtmlOfPageWithHbsWithNullModel("password.hbs"),
                getHtmlOfUnAuthorisedRequestToPage("/entries/edit/somePage"));
    }

    @Test
    public void authorizedRequestOnNewEntryPageShowsNewPage() throws Exception {
        // Given cookie with password
        // When get request to new entry page is made
        // Then new entry page is returned
        assertEquals(
                getHtmlOfPageWithHbsWithNullModel("new.hbs"),
                getHtmlOfAuthorisedRequestToPage("/entries/new"));
    }

    @Test
    public void authorizedRequestOnEditEntryPageWithNoEntriesShowsNotFoundPage()
            throws Exception {
        // Given cookie with password and empty DAO with no entries
        // model:
        Map<String, Object> model = new HashMap<>();
        model.put("status", 404);
        model.put("errorMessage", notFoundMessage);
        // When get request to edit entry page is made
        // Then not-found error page is returned
        assertEquals(
                getHtmlOfPageWithHbsWithModel("not-found.hbs", model),
                getHtmlOfAuthorisedRequestToPage("/entries/edit/someEntry"));
    }
}