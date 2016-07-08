package com.teamtreehouse.blog.dao;

import com.teamtreehouse.blog.exception.DaoException;
import com.teamtreehouse.blog.model.Comment;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;

import java.util.List;

public class Sql2oEntryDao implements EntryDao {
    private final Sql2o mSql2o;

    public Sql2oEntryDao(Sql2o sql2o) {
        mSql2o = sql2o;
    }

    @Override
    public void addComment(Comment comment) throws DaoException{
        String sqlQuery = "INSERT INTO " +
                "comments(entry_id, author, body, date)" +
                "VALUES(:entryId, :author, :body, :date)";
        try (Connection connection = mSql2o.open()) {
            connection.createQuery(sqlQuery);
            int id = (int) connection.createQuery(sqlQuery)
                    .bind(comment)
                    .executeUpdate()
                    .getKey();
            comment.setId(id);
        } catch (Sql2oException sql2oException) {
            throw new DaoException(sql2oException, "Problem adding comment");
        }
    }

    @Override
    public List<Comment> findAll() {
        String sqlQuery = "SELECT * FROM comments";
        try (Connection connection = mSql2o.open()) {
            return connection.createQuery(sqlQuery)
                    .executeAndFetch(Comment.class);
        }
    }

    @Override
    public List<Comment> findByEntryId(int entryId) {
        String sqlQuery = "SELECT * FROM comments" +
                " WHERE entry_id = :entry_id";
        try (Connection connection = mSql2o.open()) {
            return connection.createQuery(sqlQuery)
                    .addColumnMapping("ENTRY_ID", "entryId")
                    .addParameter("entry_id", entryId)
                    .executeAndFetch(Comment.class);
        }
    }

}
