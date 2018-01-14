package com.android.zhxie.pittsourcejobsearch;


public class PostingsItem {

    public final String url;
    public final String title;
    public final String datePosted;
    public final String text;
//    public double pay;

    public PostingsItem(String url, String title, String datePosted, String text) {
        this.url = url;
        this.title = title;
        this.datePosted = datePosted;
        this.text = text;
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("(")
                .append(url)
                .append(", ")
                .append(title)
                .append(", ")
                .append(datePosted)
                .append(", ")
                .append(text)
                .append(")");
        return builder.toString();
    }

}