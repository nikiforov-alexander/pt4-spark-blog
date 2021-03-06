package com.teamtreehouse.blog.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Comment {
    // entry mId
    private int mEntryId;
    public int getEntryId() {
        return mEntryId;
    }
    public void setEntryId(int entryId) {
        this.mEntryId = entryId;
    }

    // comment mId, unique
    private int mId;
    public int getId() {
        return mId;
    }
    public void setId(int id) {
        this.mId = id;
    }

    // body of a comment, required field in form
    private String mBody;
    public void setBody(String body) {
        mBody = body;
    }
    public String getBody() {
        return mBody;
    }

    // Date field, has two additional helpful methods
    private Date mDate;
    // returns String with date in html format, see Date class
    public String getHtmlCreationDate() {
        DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd hh:mm");
        return dateFormat.format(mDate);
    }
    // returns date in format like in original // index.html
    public String getCreationDateString() {
        DateFormat dateFormat = new SimpleDateFormat("LLLL dd, YYYY 'at' hh:mm");
        return dateFormat.format(mDate);
    }
    public Date getDate() {
        return mDate;
    }
    public void setDate(Date date) {
        mDate = date;
    }

    // author field
    private String mAuthor;
    public String getAuthor() {
        return mAuthor;
    }
    public void setAuthor(String author) {
        mAuthor = author;
    }

    // Default constructor
    public Comment(int entryId, String body, Date date, String author) {
        this.mEntryId = entryId;
        mBody = body;
        mDate = date;
        mAuthor = author;
    }
    // constructor used in main
    public Comment(int entryId, String body, String author) {
        this(entryId, body, new Date(), author);
    }

    @Override
    public String toString() {
        return "Comment { " +
                "mBody = '" + mBody + '\'' +
                ", mDate = " + mDate +
                ", mAuthor = '" + mAuthor + '\'' +
                " }";
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Comment comment = (Comment) o;

        if (mDate != null ? !mDate.equals(comment.mDate) : comment.mDate != null)
            return false;
        return mAuthor != null ? mAuthor.equals(comment.mAuthor) : comment.mAuthor == null;

    }
    @Override
    public int hashCode() {
        int result = mDate != null ? mDate.hashCode() : 0;
        result = 31 * result + (mAuthor != null ? mAuthor.hashCode() : 0);
        return result;
    }

}
