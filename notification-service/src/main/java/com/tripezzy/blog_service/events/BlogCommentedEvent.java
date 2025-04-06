package com.tripezzy.blog_service.events;

public class BlogCommentedEvent {

    private Long blog;
    private Long user;
    private Long comment;

    public BlogCommentedEvent() {
    }

    public BlogCommentedEvent(Long blog, Long user, Long comment) {
        this.blog = blog;
        this.user = user;
        this.comment = comment;
    }

    public Long getBlog() {
        return blog;
    }

    public void setBlog(Long blog) {
        this.blog = blog;
    }

    public Long getUser() {
        return user;
    }

    public void setUser(Long user) {
        this.user = user;
    }

    public Long getComment() {
        return comment;
    }

    public void setComment(Long comment) {
        this.comment = comment;
    }
}
