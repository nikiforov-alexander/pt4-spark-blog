package com.teamtreehouse.blog.dao;

import com.teamtreehouse.blog.exception.DaoException;
import com.teamtreehouse.blog.model.BlogEntry;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;

import java.util.List;

public class Sql2oBlogDao implements BlogDao {
    // our database object
    private final Sql2o mSql2o;

    // default constructor
    public Sql2oBlogDao(Sql2o sql2o) {
        this.mSql2o = sql2o;
    }

    @Override
    public void addEntry(BlogEntry blogEntry) throws DaoException {
        String sqlQuery
                = "INSERT INTO " +
                "entries(title, body, date) " +
                "VALUES (:title, :body, :date)";
        try (Connection connection = mSql2o.open()) {
            int id = (int) connection.createQuery(sqlQuery)
                    .bind(blogEntry)
                    .executeUpdate()
                    .getKey();
            blogEntry.setId(id);
        } catch (Sql2oException sql2oException) {
            throw new DaoException(sql2oException, "Problem adding entry");
        }
    }

    @Override
    public List<BlogEntry> findAllEntries() {
        String sqlQuery = "SELECT * FROM entries";
        try (Connection connection = mSql2o.open()) {
            return connection.createQuery(sqlQuery)
                    .executeAndFetch(BlogEntry.class);
        }
    }

    @Override
    public BlogEntry findEntryById(int id) {
        String sqlQuery = "SELECT * FROM entries WHERE id = :id";
        try (Connection connection = mSql2o.open()) {
            return connection.createQuery(sqlQuery)
                    .addParameter("id", id)
                    .executeAndFetchFirst(BlogEntry.class);
        }
    }

    @Override
    public void removeEntryById(int id) throws DaoException {
        String sqlQuery
                = "DELETE FROM " +
                "entries "+
                "WHERE id = :id";
        try (Connection connection = mSql2o.open()) {
            connection.createQuery(sqlQuery)
                    .addParameter("id", id)
                    .executeUpdate();
        } catch (Sql2oException sql2oException) {
            throw new DaoException(sql2oException, "Problem removing entry");
        }
    }
}
