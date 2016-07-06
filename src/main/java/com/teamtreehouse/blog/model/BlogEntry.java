package com.teamtreehouse.blog.model;

import com.github.slugify.Slugify;

import java.io.IOException;

public class BlogEntry {

    // Body of blog entry
    private String mBody;
    public String getBody() {
        return mBody;
    }
    public void setBody(String body) {
        mBody = body;
    }

    // Title from entry, can't be null, is slugified later to be used in
    // address of web page
    private String mTitle;
    public String getTitle() {
        return mTitle;
    }
    public void setTitle(String title) {
        mTitle = title;
    }
    // slug from title used in generating address with entry
    public String getSlugFromTitle() {
        String slug = "";
        try {
            Slugify slugify = new Slugify();
            slug = slugify.slugify(mTitle);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return slug;
    }

    // Our own Date, inheriting java Date
    private Date mCreationDate;
    public void setCreationDate(Date creationDate) {
        mCreationDate = creationDate;
    }
    public Date getCreationDate() {
        return mCreationDate;
    }
    // returns date in format accepted by HTML, tried to do like in original
    // index.html, see Date class
    public String getHtmlCreationDate() {
        return mCreationDate.getHtmlCreationDate();
    }
    // returns date in format like in original index.html, see Date class
    public String getCreationDateString() {
        return mCreationDate.getCreationDateString();
    }

    // default constructor, used to create new blog entry without comments
    public BlogEntry(String title, String body) {
        mTitle = title;
        mBody = body;
        mCreationDate = new Date();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BlogEntry blogEntry = (BlogEntry) o;

        if (mTitle != null ? !mTitle.equals(blogEntry.mTitle) : blogEntry.mTitle != null)
            return false;
        return mCreationDate != null ? mCreationDate.equals(blogEntry.mCreationDate) : blogEntry.mCreationDate == null;
    }
    @Override
    public int hashCode() {
        int result = mTitle != null ? mTitle.hashCode() : 0;
        result = 31 * result + (mCreationDate != null ? mCreationDate.hashCode() : 0);
        return result;
    }
    @Override
    public String toString() {
        return "BlogEntry { " +
                "mCreationDate = " + mCreationDate +
                ", mTitle = '" + mTitle + '\'' +
                ", mBody = '" + mBody + '\'' +
                " }";
    }

}
