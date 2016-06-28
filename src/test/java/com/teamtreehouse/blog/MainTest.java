package com.teamtreehouse.blog;

import com.teamtreehouse.blog.dao.SimpleBlogEntryDAO;
import com.teamtreehouse.blog.testing.ApiClient;
import com.teamtreehouse.blog.testing.ApiResponse;
import org.junit.*;
import spark.ModelAndView;
import spark.Spark;
import spark.template.handlebars.HandlebarsTemplateEngine;

import org.junit.Test;

import static org.junit.Assert.*;

public class MainTest {
    public static final String PORT = "4568";
    private ApiClient mApiClient;
    private SimpleBlogEntryDAO mSimpleBlogEntryDAO;

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
    private String getHtmlOfUnathorisedRequestToPage(String pageUri) {
        ApiResponse apiResponse =
                mApiClient.request("GET", "/entries/new");
        return apiResponse.getBody();
    }

    @Test
    public void unauthorisedRequestOnNewEntryPageRedirectsToPasswordPage()
        throws Exception {
        // Given no cookies with password
        // When get to new entries page
        // password html page should come as a response
        assertEquals(
                getHtmlOfPageWithHbsWithNullModel("password.hbs"),
                getHtmlOfUnathorisedRequestToPage("/entries/new"));
    }
    @Test
    public void unauthorisedRequestOnEditEntryPageRedirectsToPasswordPage()
            throws Exception {
        // Given no cookies with password
        // When get to new entries page
        // Then password html page should come as a response, even if there are
        // no such entry
        assertEquals(
                getHtmlOfPageWithHbsWithNullModel("password.hbs"),
                getHtmlOfUnathorisedRequestToPage("/entries/edit/somePage"));
    }
}