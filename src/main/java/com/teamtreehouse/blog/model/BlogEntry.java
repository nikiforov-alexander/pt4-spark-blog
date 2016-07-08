package com.teamtreehouse.blog.model;

import com.github.slugify.Slugify;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.io.IOException;

public class BlogEntry {
    // primary key in database
    private int mId;
    public int getId() {
        return mId;
    }
    public void setId(int id) {
        mId = id;
    }

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
    private Date mDate;
    public void setDate(Date date) {
        mDate = date;
    }
    public Date getDate() {
        return mDate;
    }
    // returns date in format accepted by HTML, tried to do like in original
    // index.html
    public String getHtmlCreationDate() {
        DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd hh:mm");
        return dateFormat.format(mDate);
    }
    // returns date in format like in original index.html
    public String getCreationDateString() {
        DateFormat dateFormat = new SimpleDateFormat("LLLL dd, YYYY 'at' hh:mm");
        return dateFormat.format(mDate);
    }

    // default constructor, used to create new blog entry without comments
    public BlogEntry(String title, String body) {
        this(title, body, new Date());
    }
    public BlogEntry(String title, String body, Date creationDate) {
        mTitle = title;
        mBody = body;
        mDate = creationDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BlogEntry blogEntry = (BlogEntry) o;

        if (mId != blogEntry.mId) return false;
        if (mBody != null ? !mBody.equals(blogEntry.mBody) : blogEntry.mBody != null)
            return false;
        if (!mTitle.equals(blogEntry.mTitle)) return false;
        return mDate.equals(blogEntry.mDate);

    }

    @Override
    public int hashCode() {
        int result = mId;
        result = 31 * result + (mBody != null ? mBody.hashCode() : 0);
        result = 31 * result + mTitle.hashCode();
        result = 31 * result + mDate.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "BlogEntry { " +
                "mDate = " + mDate +
                ", mTitle = '" + mTitle + '\'' +
                ", mBody = '" + mBody + '\'' +
                " }";
    }

}
