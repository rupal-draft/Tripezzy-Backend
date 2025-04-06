package com.tripezzy.blog_service.events;

public class BlogLikedEvent {

    private Long blog;

    private Long user;

    public BlogLikedEvent() {
    }

    public BlogLikedEvent(Long blog, Long user) {
        this.blog = blog;
        this.user = user;
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
}
