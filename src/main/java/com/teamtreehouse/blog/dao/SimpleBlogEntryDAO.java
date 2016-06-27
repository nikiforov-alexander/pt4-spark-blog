package com.teamtreehouse.blog.dao;

import com.teamtreehouse.blog.model.BlogEntry;
import com.teamtreehouse.blog.exception.NotFoundException;

import java.util.ArrayList;
import java.util.List;

public class SimpleBlogEntryDAO implements BlogDao {
    private List<BlogEntry> mBlogEntries;

    public SimpleBlogEntryDAO() {
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
                .filter(blogEntry -> blogEntry.getSlug().equals(slug))
                .findFirst()
                .orElseThrow(NotFoundException::new);
    }

    @Override
    public boolean containsEntry(BlogEntry blogEntry) {
        return mBlogEntries.contains(blogEntry);
    }

    @Override
    public boolean removeEntry(BlogEntry blogEntry) {
        return mBlogEntries.remove(blogEntry);
    }
}
