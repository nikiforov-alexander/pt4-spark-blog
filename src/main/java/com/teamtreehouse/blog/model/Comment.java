package com.teamtreehouse.blog.model;

public class Comment implements Comparable<Comment> {

    public Comment(String body, Date date, String author) {
        mBody = body;
        mDate = date;
        mAuthor = author;
    }
    public Comment(String body, String author) {
        this(body, new Date(), author);
    }

    public String getBody() {
        return mBody;
    }

    private String mBody;
    private Date mDate;
    // returns String with date in html format, see Date class
    public String getHtmlCreationDate() {
        return mDate.getHtmlCreationDate();
    }
    // returns date in format like in original // index.html
    public String getCreationDateString() {
        return mDate.getCreationDateString();
    }

    public String getAuthor() {
        return mAuthor;
    }

    private String mAuthor;

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

    @Override
    public int compareTo(Comment otherComment) {
        if (this.equals(otherComment)) {
            return 0;
        }
        return mDate.compareTo(otherComment.mDate);
    }
}
