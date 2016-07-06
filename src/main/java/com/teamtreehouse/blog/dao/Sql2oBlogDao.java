package com.teamtreehouse.blog.dao;

import com.teamtreehouse.blog.model.BlogEntry;
import com.teamtreehouse.blog.exception.NotFoundException;

import java.util.ArrayList;
import java.util.List;

public class Sql2oBlogDao implements BlogDao {
    private List<BlogEntry> mBlogEntries;

    public Sql2oBlogDao() {
       mBlogEntries = new ArrayList<>();
    }

    @Override
    public boolean addEntry(BlogEntry blogEntry) {
        return mBlogEntries.add(blogEntry);
    }

    @Override
    public List<BlogEntry> findAllEntries() {
        return new ArrayList<>(mBlogEntries);
    }

    @Override
    public BlogEntry findEntryByHashId(String hashId)
            throws NotFoundException {
        return null;
    }

    @Override
    public boolean removeEntry(BlogEntry blogEntry) {
        return mBlogEntries.remove(blogEntry);
    }
}
