package com.teamtreehouse.blog.model;

import com.github.slugify.Slugify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlogEntry {
    private Set<String> mTags;
    public Set<String> getTags() {
        return mTags;
    }

    private List<Comment> mComments;
    public List<Comment> getComments() {
        return mComments;
    }

    private String mBody;
    public String getBody() {
        return mBody;
    }
    public void setBody(String body) {
        mBody = body;
    }


    private String mTitle;
    public String getTitle() {
        return mTitle;
    }
    public void setTitle(String title) {
        mTitle = title;
    }
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

    private String mHashId;
    public String getHashId() {
        return mHashId;
    }


    private Date mCreationDate;
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

    public boolean addComment(Comment comment) {
        // Store these comments!
        // Here we totally rely on underlying add method, no testing
        return mComments.add(comment);
    }

    // used in default constructor, generates unique hashId
    private void setSlugUsingTitleAndCreationDate() {
        try {
            Slugify slugify = new Slugify();
            mHashId = slugify.slugify(hashCode() + "");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    // default constructor, used to create new blog entry without comments
    public BlogEntry(String title, String body) {
        mComments = new ArrayList<>();
        mTitle = title;
        mBody = body;
        mCreationDate = new Date();
        setSlugUsingTitleAndCreationDate();
        mTags = new HashSet<>();
    }
    // constructor used in edit entry page, to save comments from old entry
    public BlogEntry(String title, String body, List<Comment> comments) {
        mComments = new ArrayList<>();
        mTitle = title;
        mBody = body;
        mCreationDate = new Date();
        setSlugUsingTitleAndCreationDate();
        // now i hope here, that I shouldn't type mComments = new Set(comments)
        // because when we remove later on old blog entry
        mComments = comments;
        mTags = new HashSet<>();
    }
    protected void slugifyTagsStringAndAddToTagsMember(String tagsString) {
        Pattern pattern = Pattern.compile("([a-zA-Z0-9-_]+)");
        Matcher matcher = pattern.matcher(tagsString);
        while (matcher.find()) {
            String tag = matcher.group();
            try {
                Slugify slugify = new Slugify();
                mTags.add(slugify.slugify(tag));
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
    // constructor used in edit entry page, to save comments from old entry
    // with tags
    public BlogEntry(
            String title,
            String body,
            List<Comment> comments,
            String tagsString) {
        mTitle = title;
        mBody = body;
        mCreationDate = new Date();
        setSlugUsingTitleAndCreationDate();
        // now i hope here, that I shouldn't type mComments = new Set(comments)
        // because when we remove later on old blog entry
        mComments = comments;
        // create new tags set, because we don't want same tags
        mTags = new HashSet<>();
        // slugify tags
        slugifyTagsStringAndAddToTagsMember(tagsString);
    }

    // Blog entries will be equal if body and title are same, otherwise

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
    // we change blog entry in edit/save page

    // decided for title to be included in equals

    @Override
    public String toString() {
        return "BlogEntry { " +
                "mCreationDate = " + mCreationDate +
                ", mTitle = '" + mTitle + '\'' +
                ", mBody = '" + mBody + '\'' +
                " }";
    }

}
