package com.hong2.alpha.models;

import java.util.List;

/**
 * Created by User on 8/22/2017.
 */

public class Comment {

    private String comment;
    private String user_id;
    private String file_url;
    private List<Like> likes;
    private String date_created;

    public Comment() {

    }

    public Comment(String comment, String user_id, List<Like> likes, String date_created) {
        this.comment = comment;
        this.user_id = user_id;
        this.likes = likes;
        this.date_created = date_created;
        this.file_url = "";
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setFile_url(String uri)
    {
        this.file_url = uri;
    }
    public String getFile_url() { return file_url; }
    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public List<Like> getLikes() {
        return likes;
    }

    public void setLikes(List<Like> likes) {
        this.likes = likes;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "comment='" + comment + '\'' +
                ", user_id='" + user_id + '\'' +
                ", likes=" + likes +
                ", date_created='" + date_created + '\'' +
                ", file_url='" + file_url + '\'' +
                '}';
    }
}
