package com.teamtreehouse.blog.dao;

import com.github.slugify.Slugify;
import com.teamtreehouse.blog.exception.DaoException;
import com.teamtreehouse.blog.model.Tag;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Sql2oTagsDao implements TagsDao {
    private final Sql2o mSql2o;

    public Sql2oTagsDao(Sql2o sql2o) {
        mSql2o = sql2o;
    }

    @Override
    public void addTagsFromStringProcessing(String tagsString, int entryId)
            throws DaoException {
        Pattern pattern = Pattern.compile("([a-zA-Z0-9-_]+)");
        Matcher matcher = pattern.matcher(tagsString);
        while (matcher.find()) {
            String rawTagName = matcher.group();
            try {
                Slugify slugify = new Slugify();
                String slugFromTagName = slugify.slugify(rawTagName);
                Tag tag = new Tag(entryId, slugFromTagName);
                addTag(tag);
            } catch (IOException ioe) {
                throw new DaoException(ioe, "Problem adding tags");
            }
        }
    }

    @Override
    public void addTag(Tag tag) throws DaoException{
        String sqlQuery = "INSERT INTO " +
                "tags(entry_id, name)" +
                "VALUES(:entryId, :name)";
        try (Connection connection = mSql2o.open()) {
            connection.createQuery(sqlQuery);
            int id = (int) connection.createQuery(sqlQuery)
                    .bind(tag)
                    .executeUpdate()
                    .getKey();
            tag.setId(id);
        } catch (Sql2oException sql2oException) {
            throw new DaoException(sql2oException, "Problem adding tag");
        }
    }

    @Override
    public List<Tag> findAll() {
        String sqlQuery = "SELECT * FROM tags";
        try (Connection connection = mSql2o.open()) {
            return connection.createQuery(sqlQuery)
                    .addColumnMapping("ENTRY_ID", "entryId")
                    .executeAndFetch(Tag.class);
        }
    }

    @Override
    public List<Tag> findByEntryId(int entryId) {
        String sqlQuery = "SELECT * FROM tags" +
                " WHERE entry_id = :entry_id";
        try (Connection connection = mSql2o.open()) {
            return connection.createQuery(sqlQuery)
                    .addColumnMapping("ENTRY_ID", "entryId")
                    .addParameter("entry_id", entryId)
                    .executeAndFetch(Tag.class);
        }
    }
}
