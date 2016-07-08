package com.teamtreehouse.blog.dao;

import com.teamtreehouse.blog.exception.DaoException;
import com.teamtreehouse.blog.model.BlogEntry;
import com.teamtreehouse.blog.model.Comment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import static org.junit.Assert.*;

public class Sql2oEntryDaoTest {
    private Sql2oEntryDao mSql2oEntryDao;
    private Sql2o mSql2o;
    private Connection mConnection;
    private Sql2oBlogDao mSql2oBlogDao;
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
        mSql2oEntryDao = new Sql2oEntryDao(mSql2o);
        // create test blog entry
        mTestBlogEntry = new BlogEntry("Title", "Body");
        // add test course to Blog Dao
        mSql2oBlogDao.addEntry(mTestBlogEntry);
    }

    @After
    public void tearDown() throws Exception {
        mConnection.close();
    }

    private Comment newTestComment() {
        return new Comment(mTestBlogEntry.getId(), "Body", "Author");
    }

    @Test
    public void addingCommentSetsId() throws Exception {
        // Given blog DAO with one entry, comment attached to this
        // entry
        Comment comment = newTestComment();
        // When comment is added to entry dao
        mSql2oEntryDao.addComment(comment);
        // Then id of newly added comment should be equal to 1
        assertEquals(1, comment.getId());
    }

    @Test
    public void multipleCommentsFoundIfTheyExistForEntry() throws Exception {
        // Given blog DAO with one entry and two comments attached
        Comment comment1 =
                new Comment(mTestBlogEntry.getId(), "Body1", "Author1");
        Comment comment2 = newTestComment();
        // When two comments are added
        mSql2oEntryDao.addComment(comment1);
        mSql2oEntryDao.addComment(comment2);
        // Then number of comments in dao should be 2
        int numberOfCommentsAttachedToTestEntry =
                mSql2oEntryDao.findByEntryId(mTestBlogEntry.getId()).size();
        assertEquals(2, numberOfCommentsAttachedToTestEntry);
    }
    @Test(expected = DaoException.class)
    public void addingCommentToNonExistingCourseFails() throws Exception {
        Comment comment = new Comment(12, "body", "author");
        mSql2oEntryDao.addComment(comment);
    }

    @Test
    public void sizeOfDaoIsEqualToOneAfterAddingOneComment() throws Exception {
        // Given one test comment associated with first blog entry
        Comment comment = newTestComment();
        // When we add comment to DAO
        mSql2oEntryDao.addComment(comment);
        // Then size of List of comments returned using findAll should be 1
        assertEquals(1, mSql2oEntryDao.findAll().size());
    }
}