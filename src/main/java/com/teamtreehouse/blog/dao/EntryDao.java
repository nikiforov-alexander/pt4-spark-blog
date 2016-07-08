package com.teamtreehouse.blog.dao;

import com.teamtreehouse.blog.exception.DaoException;
import com.teamtreehouse.blog.model.Comment;

import java.util.List;

public interface EntryDao {
    void addComment(Comment comment) throws DaoException;
    List<Comment> findAll();
    List<Comment> findByEntryId(int entryId);
}
