package com.teamtreehouse.blog;

import com.teamtreehouse.blog.testing.ApiClient;
import com.teamtreehouse.blog.testing.ApiResponse;
import org.junit.*;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
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
    private static final String mCookieWithPassword = "password=admin";

    @Rule
    public TestRule watcher = new TestWatcher() {
        protected void starting(Description description) {
            System.out.printf("%n -------- Starting test: %s %n",description.getMethodName());
        }
    };

    @BeforeClass
    public static void startServer() {
        String[] args = {PORT};
        Main.main(args);
        Spark.awaitInitialization();
    }
    @AfterClass
    public static void stopServer() {
        Spark.stop();
    }

    @Before
    public void setUp() throws Exception {
        mApiClient = new ApiClient("http://localhost:" + PORT);
    }

//    @Test
//    public void emptyDaoPageIsTheSameAsModeled() throws Exception {
//        HandlebarsTemplateEngine handlebarsTemplateEngine =
//                new HandlebarsTemplateEngine();
//        String indexHtml = handlebarsTemplateEngine
//                .render(new ModelAndView(null, "index.hbs"));
//        ApiResponse apiResponse =
//                mApiClient.request("GET", "/");
//        assertEquals(indexHtml, apiResponse.getBody());
//    }

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
                getHtmlOfAuthorisedRequestToPage("/entries/edit/1234543/title"));
    }

    // Not quite right test, but I will still include it
    @Test
    public void addingNewEntryWithPostRequestReturnsRightResponseHomePageWithNewEntry()
            throws Exception {
        // Given initial DAO with 3 test entries, model of html created by
        // handlebars template engine, with all entries from POST request

        // When POST request to new page is made, and new DAO is used to
        // create index page
        ApiResponse apiResponse =
                mApiClient.request("POST",
                        "/entries/new",
                        "title=title&body=body",
                        mCookieWithPassword);
        Map<String, Object> model = new HashMap<>();
        model.put("entries", Main.mSimpleBlogEntryDAO.findAllEntries());
        // then body of response should be same as of index page with simple
        // blog entry dao given
        assertEquals(
                getHtmlOfPageWithHbsWithModel("index.hbs", model),
                apiResponse.getBody()
        );
    }
}