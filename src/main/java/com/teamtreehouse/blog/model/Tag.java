package com.teamtreehouse.blog.model;

public class Tag {
    // primary key
    private int mId;
    public int getId() {
        return mId;
    }
    public void setId(int id) {
        mId = id;
    }

    // our foreign key
    private int mEntryId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tag tag = (Tag) o;

        if (mId != tag.mId) return false;
        if (mEntryId != tag.mEntryId) return false;
        return mName != null ? mName.equals(tag.mName) : tag.mName == null;

    }

    @Override
    public int hashCode() {
        int result = mId;
        result = 31 * result + mEntryId;
        result = 31 * result + (mName != null ? mName.hashCode() : 0);
        return result;
    }

    public int getEntryId() {
        return mEntryId;
    }
    public void setEntryId(int entryId) {
        mEntryId = entryId;
    }

    // main field, tag name
    private String mName;
    public String getName() {
        return mName;
    }
    public void setName(String name) {
        mName = name;
    }

    @Override
    public String toString() {
        return mName;
    }

    // default constructor
    public Tag(int entryId, String name) {
        mEntryId = entryId;
        mName = name;
    }
}
