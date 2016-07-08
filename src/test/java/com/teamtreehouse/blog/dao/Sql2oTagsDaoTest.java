package com.teamtreehouse.blog.dao;

import com.teamtreehouse.blog.exception.DaoException;
import com.teamtreehouse.blog.model.BlogEntry;
import com.teamtreehouse.blog.model.Tag;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import static org.junit.Assert.assertEquals;

public class Sql2oTagsDaoTest {

    private Sql2o mSql2o;
    private Connection mConnection;
    private Sql2oBlogDao mSql2oBlogDao;
    private Sql2oTagsDao mSql2oTagsDao;
    private BlogEntry mTestBlogEntry;

    @Before
    public void setUp() throws Exception {
        // create sql2o object
        String connectionString = "jdbc:h2:mem:testing;" +
                "INIT=RUNSCRIPT from 'classpath:db/init.sql'";
        mSql2o = new Sql2o(connectionString, "", "");
        // open connection
        mConnection = mSql2o.open();
        // create interacting DAOs
        mSql2oBlogDao = new Sql2oBlogDao(mSql2o);
        mSql2oTagsDao = new Sql2oTagsDao(mSql2o);
        // create test blog entry
        mTestBlogEntry = new BlogEntry("Title", "Body");
        // add test course to Blog Dao
        mSql2oBlogDao.addEntry(mTestBlogEntry);
    }

    @After
    public void tearDown() throws Exception {
        mConnection.close();
    }

    private Tag newTestTag() {
        return new Tag(mTestBlogEntry.getId(), "tag");
    }

    @Test
    public void addingTagSetsId() throws Exception {
        // Given blog DAO with one entry, tag attached to this
        // entry
        Tag tag = newTestTag();
        // When tag is added to dao
        mSql2oTagsDao.addTag(tag);
        // Then id of newly added comment should be equal to 1
        assertEquals(1, tag.getId());
    }

    @Test
    public void multipleTagsFoundIfTheyExistForEntry() throws Exception {
        // Given blog DAO with one entry and two tags attached
        Tag tag1 =
                new Tag(mTestBlogEntry.getId(),"Tag1");
        Tag tag2 =
                new Tag(mTestBlogEntry.getId(),"Tag2");
        // When two comments are added
        mSql2oTagsDao.addTag(tag1);
        mSql2oTagsDao.addTag(tag2);
        // Then number of comments in dao should be 2
        int numberOfCommentsAttachedToTestEntry =
                mSql2oTagsDao.findByEntryId(mTestBlogEntry.getId()).size();
        assertEquals(2, numberOfCommentsAttachedToTestEntry);
    }
    @Test(expected = DaoException.class)
    public void addingTagToNonExistingCourseFails() throws Exception {
        Tag tag = new Tag(12, "tag");
        mSql2oTagsDao.addTag(tag);
    }

    @Test
    public void sizeOfDaoIsEqualToOneAfterAddingOneTag() throws Exception {
        // Given one test tag associated with first blog entry
        Tag tag = newTestTag();
        // When we add tag to DAO
        mSql2oTagsDao.addTag(tag);
        // Then size of List of tags returned using findAll should be 1
        assertEquals(1, mSql2oTagsDao.findAll().size());
    }

    @Test
    public void addingStringWithThreeTagsWillAddThreeTags() throws Exception {
        // Given string with tags, empty tags DAO, one test blog entry
        String stringWithTags = "tag-1 Tag2 tag3_234";
        // when we add tags to DAO with test blog entry id
        mSql2oTagsDao.addTagsFromStringProcessing(
                stringWithTags, mTestBlogEntry.getId());
        // Then size of tags dao should be equal to 3
        assertEquals(3, mSql2oTagsDao.findAll().size());
    }
}
