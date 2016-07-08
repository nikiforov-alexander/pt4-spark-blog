package com.teamtreehouse.blog.dao;

import com.teamtreehouse.blog.model.BlogEntry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.List;

import static org.junit.Assert.*;

public class Sql2oBlogDaoTest {
    private Sql2oBlogDao mSql2oBlogDao;
    private Connection mConnection;
    @Before
    public void setUp() throws Exception {
        String connectionString =
                "jdbc:h2:mem:testing;" +
                        "INIT=RUNSCRIPT from 'classpath:db/init.sql'";
        Sql2o sql2o = new Sql2o(connectionString, "", "");
        mSql2oBlogDao = new Sql2oBlogDao(sql2o);
        // Keep connection open through entire test so that it is not wiped out
        mConnection = sql2o.open();
    }

    @After
    public void tearDown() throws Exception {
        mConnection.close();
    }

    private BlogEntry newTestBlogEntry() {
        return new BlogEntry("Title", "Body");
    }

    @Test
    public void existingEntriesCanBeFoundById() throws Exception {
        // Given test blog entry, empty sql2oBlogDao
        BlogEntry blogEntry = newTestBlogEntry();
        // When we add test entry to dao
        mSql2oBlogDao.addEntry(blogEntry);
        // Then found entry by id in our dao, should be equal to our test
        // blog entry
        BlogEntry foundBlogEntry =
                mSql2oBlogDao.findEntryById(blogEntry.getId());
        System.out.println(foundBlogEntry);
        assertEquals(blogEntry, foundBlogEntry);
    }

    @Test
    public void addingEntrySetsId() throws Exception {
        // Given test blog entry, empty DAO
        BlogEntry blogEntry = newTestBlogEntry();
        // When entry is added to DAO
        mSql2oBlogDao.addEntry(blogEntry);
        // Then his id should be incremented to one
        assertEquals(1, blogEntry.getId());
    }

    @Test
    public void addedEntryIsReturnedFromFindAll() throws Exception {
        // Given DAO with one test entry
        BlogEntry blogEntry = newTestBlogEntry();
        mSql2oBlogDao.addEntry(blogEntry);
        // When findAllEntries() is executed
        List<BlogEntry> listOfAllEntries =
                mSql2oBlogDao.findAllEntries();
        // Then size of this list should be equal to one
        assertEquals(1, listOfAllEntries.size());
    }
}