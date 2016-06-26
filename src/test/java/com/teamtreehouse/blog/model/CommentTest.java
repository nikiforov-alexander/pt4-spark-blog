package com.teamtreehouse.blog.model;

import org.junit.Test;


import static org.junit.Assert.*;

public class CommentTest {
    @Test
    public void commentWithLaterDateUponComparisonReturnsLess()
            throws Exception {
        // Given one comment, and the other one - one second away
        Comment earlierComment =
                new Comment("earlierComment", new Date(1L), "John Doe");
        Comment laterComment =
                new Comment("laterComment", new Date(2L), "John Doe");
        // When comments are compared
        int comparisonInt = laterComment.compareTo(earlierComment);
        // Then 1 should be returned, when later is compared to earlier
        assertEquals(1, comparisonInt);
    }
}