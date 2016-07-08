package com.teamtreehouse.blog.dao;

import com.teamtreehouse.blog.exception.DaoException;
import com.teamtreehouse.blog.model.Tag;

import java.util.List;

public interface TagsDao {
    void addTagsFromStringProcessing(String tagsString, int entryId) throws DaoException;
    void addTag(Tag tag) throws DaoException;
    List<Tag> findAll();
    List<Tag> findByEntryId(int entryId);
}
