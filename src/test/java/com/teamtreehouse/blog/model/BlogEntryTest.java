package com.teamtreehouse.blog.model;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class BlogEntryTest {
    @Test
    public void rightSetOfTagsIsCreatedFromBlogEntryConstructorWithStringWithTags()
            throws Exception {
        // Given string with tags, and right set of tags
        String stringWithTags = " tag1, tag2  tag2;. tag4  ";
        Set<String> setOfFourStrings = new HashSet<>();
        setOfFourStrings.add("tag1");
        setOfFourStrings.add("tag2");
        setOfFourStrings.add("tag4");
        // When blog entry constructed from it
        BlogEntry blogEntry = new BlogEntry(
                        "title",
                        "body",
                        new ArrayList<>(),
                        stringWithTags);
        // Then mTags of BlogEntry should be equal to right Set
        assertEquals(setOfFourStrings, blogEntry.getTags());
    }
}