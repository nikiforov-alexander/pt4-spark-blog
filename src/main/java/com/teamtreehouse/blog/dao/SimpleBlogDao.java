package com.teamtreehouse.blog.dao;

import com.teamtreehouse.blog.model.BlogEntry;
import com.teamtreehouse.blog.exception.NotFoundException;

import java.util.ArrayList;
import java.util.List;

public class SimpleBlogDao implements BlogDao {
    private List<BlogEntry> mBlogEntries;

    public SimpleBlogDao() {
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
    public BlogEntry findEntryBySlug(String slug) {
        return mBlogEntries.stream()
                .filter(blogEntry -> blogEntry.getHashId().equals(slug))
                .findFirst()
                .orElseThrow(NotFoundException::new);
    }

    @Override
    public boolean removeEntry(BlogEntry blogEntry) {
        return mBlogEntries.remove(blogEntry);
    }
}
