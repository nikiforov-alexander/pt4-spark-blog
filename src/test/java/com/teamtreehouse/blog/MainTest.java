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
    private static final String PORT = "4568";
    private static final String notFoundMessage = "No such entry found";
    private ApiClient mApiClient;
    private static final String mCookieWithPassword = "password=admin";
    private HashMap<String, Object> mErrorPageModel;

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
        mErrorPageModel = new HashMap<>();
        mErrorPageModel.put("status", 404);
        mErrorPageModel.put("errorMessage", notFoundMessage);
    }

    // methods to get html page as a string giving a .hbs file name and model
    // to put, null or filled
    // @return String - rendered html with Handlebars template engine
    private String getHtmlOfPageWithHbsWithNullModel(String hbsFileName) {
        HandlebarsTemplateEngine handlebarsTemplateEngine =
                new HandlebarsTemplateEngine();
        return handlebarsTemplateEngine
                .render(new ModelAndView(null, hbsFileName));
    }
    private String getHtmlOfPageWithHbsWithModel(String hbsFileName,
                                                 Map<?,?> model) {
        HandlebarsTemplateEngine handlebarsTemplateEngine =
                new HandlebarsTemplateEngine();
        return handlebarsTemplateEngine
                .render(new ModelAndView(model, hbsFileName));
    }
    // methods to get response body of GET request to given URI with or without
    // password cookie
    // @return String - body of response
    private String
        getResponseBodyOfGetRequestWithoutPasswordCookie(String pageUri) {
        return mApiClient
                .request("GET", pageUri)
                .getBody();
    }
    private String
        getResponseBodyOfGetRequestWithRightPasswordCookie(String pageUri) {
        return mApiClient
                .request("GET", pageUri, null, mCookieWithPassword)
                .getBody();
    }
    // methods to get response body of POST request to given URI with or without
    // password cookie
    // @return String - body of response
    private String
        getResponseBodyOfPostRequestWithoutPasswordCookie(String pageUri,
                                                          String requestBody) {
        return mApiClient
                .request("POST", pageUri, requestBody)
                .getBody();
    }
    private String
        getResponseBodyOfPostRequestWithRightPasswordCookie(
            String pageUri, String requestBody) {
        return mApiClient
                .request("POST", pageUri, requestBody, mCookieWithPassword)
                .getBody();
    }

    @Test
    public void unauthorisedRequestOnNewEntryPageRedirectsToPasswordPage()
        throws Exception {
        // Given no cookies with password
        // When get to new entries page
        // password html page should come as a response
        assertEquals(
                getHtmlOfPageWithHbsWithNullModel("password.hbs"),
                getResponseBodyOfGetRequestWithoutPasswordCookie("/entries/new"));
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
                getResponseBodyOfGetRequestWithoutPasswordCookie("/entries/edit/somePage"));
    }

    @Test
    public void authorizedRequestOnNewEntryPageShowsNewPage() throws Exception {
        // Given cookie with password
        // When get request to new entry page is made
        // Then new entry page is returned
        assertEquals(
                getHtmlOfPageWithHbsWithNullModel("new.hbs"),
                getResponseBodyOfGetRequestWithRightPasswordCookie("/entries/new"));
    }

    @Test
    public void authorizedRequestOnDetailEntryPageShowsNotFoundPage()
            throws Exception {
        // Given cookie with password and empty DAO with no entries
        // model:
        // When get request to edit entry page is made
        // Then not-found error page is returned
        assertEquals(
                getHtmlOfPageWithHbsWithModel("not-found.hbs", mErrorPageModel),
                getResponseBodyOfGetRequestWithRightPasswordCookie(
                        "/entries/detail/1234543/title"));
    }
    @Test
    public void authorizedRequestOnEditEntryPageWithNoEntriesShowsNotFoundPage()
            throws Exception {
        // Given cookie with password and empty DAO with no entries
        // model of error page:
        // When get request to edit entry page is made
        // Then not-found error page is returned
        assertEquals(
                getHtmlOfPageWithHbsWithModel("not-found.hbs", mErrorPageModel),
                getResponseBodyOfGetRequestWithRightPasswordCookie(
                        "/entries/edit/1234543/title"));
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

    @Test
    public void givingWrongPasswordRedirectsBackToHomePageWhenSessionIsNew()
            throws Exception {
        // Given no cookies with password, and new empty session
        // When user tries type wrong password
        // Then home page is returned back
        Map<String, Object> model = new HashMap<>();
        model.put("entries", Main.mSimpleBlogEntryDAO.findAllEntries());
        assertEquals(
                getHtmlOfPageWithHbsWithModel("index.hbs", model),
                getResponseBodyOfPostRequestWithoutPasswordCookie(
                        "/password","password=password"));
    }


}