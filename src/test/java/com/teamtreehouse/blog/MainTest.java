package com.teamtreehouse.blog;

import com.teamtreehouse.blog.dao.SimpleBlogEntryDAO;
import com.teamtreehouse.blog.testing.ApiClient;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import spark.Spark;

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

}