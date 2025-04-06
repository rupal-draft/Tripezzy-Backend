package com.tripezzy.blog_service.events;

public class BlogCreatedEvent {

    private Long blog;

    private String title;

    private Long author;

    public BlogCreatedEvent() {
    }

    public BlogCreatedEvent(Long blog, String title, Long author) {
        this.blog = blog;
        this.title = title;
        this.author = author;
    }

    public Long getBlog() {
        return blog;
    }

    public void setBlog(Long blog) {
        this.blog = blog;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getAuthor() {
        return author;
    }

    public void setAuthor(Long author) {
        this.author = author;
    }
}
