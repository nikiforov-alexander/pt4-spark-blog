package com.teamtreehouse.blog;

import com.teamtreehouse.blog.model.BlogEntry;
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
    private final String mRightPassword = "admin";

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
                        "title=title&body=body&tags=tag1",
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
        // When user tries type wrong password, ( same will happen when
        // password  is right )
        // Then home page is returned back
        Map<String, Object> model = new HashMap<>();
        model.put("entries", Main.mSimpleBlogEntryDAO.findAllEntries());
        assertEquals(
                getHtmlOfPageWithHbsWithModel("index.hbs", model),
                getResponseBodyOfPostRequestWithoutPasswordCookie(
                        "/password","password=password"));
    }

//    // test doesn't work because session is not saved, apparently we
//    // have to use WebSocket maybe ... ?
//    @Test
//    public void givingRightPasswordRedirectsBackToPageWhereHeAskedFor()
//            throws Exception {
//        // Given no cookies with password, and session with new page
//        // set after get request sent to page with new entries
//        mApiClient.request("GET", "/entries/new");
//        // When user tries type right password, making POST request at
//        // password page
//        // Then home page is returned back
//        assertEquals(
//                getHtmlOfPageWithHbsWithNullModel("new.hbs"),
//                getResponseBodyOfPostRequestWithoutPasswordCookie(
//                        "/password","password=" + mRightPassword));
//    }


    @Test
    public void detailPageReturnedRightAsWeExpected() throws Exception {
        // Given no cookies with password, no sessions
        BlogEntry firstBlogEntry =
                Main.mSimpleBlogEntryDAO.findAllEntries().get(0);
        HashMap<String, Object> model = new HashMap<>();
        model.put("entry", firstBlogEntry);
        model.put("comments", firstBlogEntry.getComments());
        model.put("tags", firstBlogEntry.getTags());
        // When we make GET request to detail page of first test Entry
        String requestBodyOfGetRequestToDetailPage =
                getResponseBodyOfGetRequestWithRightPasswordCookie(
                        "/entries/detail/"
                        + firstBlogEntry.getHashId() + "/"
                        + firstBlogEntry.getSlugFromTitle()
                );
        // Then body of detail page modeled offline using handlebars
        // should be equal to response body of actual detail page
        assertEquals(
                getHtmlOfPageWithHbsWithModel("detail.hbs", model),
                requestBodyOfGetRequestToDetailPage
        );
    }

    @Test
    public void postToEntriesDetailsPageThatDoesNotExistsReturnsNotFoundPage()
            throws Exception {
        // Given no cookies with password, no sessions, dao with three
        // test entries
        // When we make POST request to page with address that doesn't exist
        ApiResponse apiResponse =
                mApiClient.request("POST",
                        "/entries/detail/12442341/title",
                        "title=title&body=body");
        // Then body of response of this request should be equal to 404
        // modeled offline with handlebars page
        assertEquals(
                getHtmlOfPageWithHbsWithModel("not-found.hbs", mErrorPageModel),
                apiResponse.getBody()
        );

    }
    @Test
    public void postToEntriesDetailsExistingPageTryingToCreateCommentReturnsDetailPageWithNewComment()
            throws Exception {
        // Given no cookies with password, no sessions, dao with three
        // test entries
        // When we make POST request to page with address that doesn't exist
        BlogEntry firstBlogEntry =
            Main.mSimpleBlogEntryDAO.findAllEntries().get(0);
        ApiResponse apiResponse =
                mApiClient.request("POST",
                        "/entries/detail/"
                        + firstBlogEntry.getHashId() +
                        "/" + firstBlogEntry.getSlugFromTitle(),
                        "name=name&body=body");
        firstBlogEntry =
                Main.mSimpleBlogEntryDAO.findAllEntries().get(0);
        HashMap<String, Object> model = new HashMap<>();
        model.put("entry", firstBlogEntry);
        model.put("comments", firstBlogEntry.getComments());
        model.put("tags", firstBlogEntry.getTags());
        // Then body of response of this request should be equal to
        // modeled offline with handlebars page of detail entry with
        // new comment
        assertEquals(
                getHtmlOfPageWithHbsWithModel("detail.hbs", model),
                apiResponse.getBody()
        );
    }

    @Test
    public void editEntryPageIsReturnedCorrectly() throws Exception {
        // Given cookie with password, no sessions, dao with three
        // test entries, and offline page generated by us using handlebars
        // and first blog entry
        BlogEntry firstBlogEntry =
                Main.mSimpleBlogEntryDAO.findAllEntries().get(0);
        HashMap<String, Object> model = new HashMap<>();
        model.put("entry", firstBlogEntry);
        String htmlStringOfEditEntryPageRenderedOfflineWithModel =
                getHtmlOfPageWithHbsWithModel("edit.hbs", model);
        // When GET request with right password is made to edit entry page
        String bodyOfGetRequestWithRightPassword =
                getResponseBodyOfGetRequestWithRightPasswordCookie(
                        "/entries/edit/"
                        + firstBlogEntry.getHashId() + "/"
                        + firstBlogEntry.getSlugFromTitle());
        // Then response body of request should be equal to our
        // offline generated model
        assertEquals(
                htmlStringOfEditEntryPageRenderedOfflineWithModel,
                bodyOfGetRequestWithRightPassword
                );
    }

    @Test
    public void postRequestToSaveEntryReturnsCorrectHomePage() throws Exception {
        // Given cookie with password, dao with three test entries, no
        // sessions, and first entry edit page
        BlogEntry firstBlogEntry =
                Main.mSimpleBlogEntryDAO.findAllEntries().get(0);
        // When user edits entry, changing title and body
        String bodyOfResponseToPostRequestMadeToEditPage =
                getResponseBodyOfPostRequestWithRightPasswordCookie(
                        "/entries/save/" + firstBlogEntry.getHashId() + "/"
                        + firstBlogEntry.getSlugFromTitle(),
                       "title=title&body=body&tags=tag1"
                );
        Map<String, Object> model = new HashMap<>();
        model.put("entries", Main.mSimpleBlogEntryDAO.findAllEntries());
        String htmlStringOfModeledIndexPageWithNewEntry =
                getHtmlOfPageWithHbsWithModel("index.hbs", model);
        // Then body of response should be equal to new home page with changed
        // home page
        assertEquals(bodyOfResponseToPostRequestMadeToEditPage,
                htmlStringOfModeledIndexPageWithNewEntry);
    }

    @Test
    public void removingFirstEntryReturnsHomePageWithTwoEntries()
            throws Exception {
        // Given cookie with password, no session, dao with three test entries,
        // and detail page of first entry
        BlogEntry firstBlogEntry =
                Main.mSimpleBlogEntryDAO.findAllEntries().get(0);
        // When remove button is pressed, and get request to /entries/remove/...
        // is made
        String responseBodyOfGetRequestMadeWhenRemoveIsPressed =
                getResponseBodyOfGetRequestWithRightPasswordCookie(
                        "/entries/remove/" + firstBlogEntry.getHashId() + "/"
                        + firstBlogEntry.getSlugFromTitle()
                );
        HashMap<String, Object> model = new HashMap<>();
        model.put("entries", Main.mSimpleBlogEntryDAO.findAllEntries());
        String htmlStringOfPageGeneratedByHandlebarsByUs =
                getHtmlOfPageWithHbsWithModel("index.hbs", model);
        // Then user should be returned to home page, and body of the page
        // generated from changed DAO by us is equal to get response body
        assertEquals(
                htmlStringOfPageGeneratedByHandlebarsByUs,
                responseBodyOfGetRequestMadeWhenRemoveIsPressed);
    }
    @Test
    public void makingGetRequestToNonExistingEntryReturnsErrorPageAndNotServerError()
            throws Exception {
        // Given cookie with password, no session, dao with three test entries,
        // When remove button is pressed, and get request to /entries/remove/...
        // is made
        String responseBodyOfGetRequestMadeWhenRemoveIsPressed =
                getResponseBodyOfGetRequestWithRightPasswordCookie(
                        "/entries/remove/123456/title");
        String htmlStringOfErrorPageGeneratedByUs =
                getHtmlOfPageWithHbsWithModel("not-found.hbs", mErrorPageModel);
        // Then user should be returned to home page, and body of the page
        // generated from changed DAO by us is equal to get response body
        assertEquals(
                htmlStringOfErrorPageGeneratedByUs,
                responseBodyOfGetRequestMadeWhenRemoveIsPressed);
    }
}