package com.teamtreehouse.blog.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Date extends java.util.Date {
    // returns date in format accepted by HTML, tried to do like in original
    // index.html
    public String getHtmlCreationDate() {
        DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd hh:mm");
        return dateFormat.format(this);
    }
    // returns date in format like in original // index.html
    public String getCreationDateString() {
        DateFormat dateFormat = new SimpleDateFormat("LLLL dd, YYYY 'at' hh:mm");
        return dateFormat.format(this);
    }

    public Date(Long numberOfMsFromBeginning) {
        super(numberOfMsFromBeginning);
    }

    public Date() {
        super();
    }
}
