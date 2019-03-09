package com.tnsoft.web.model;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class MenuView {

    MultipartFile logo;
    MultipartFile title;
    MultipartFile background;

    public MultipartFile getLogo() {
        return logo;
    }

    public void setLogo(MultipartFile logo) {
        this.logo = logo;
    }

    public MultipartFile getTitle() {
        return title;
    }

    public void setTitle(MultipartFile title) {
        this.title = title;
    }

    public MultipartFile getBackground() {
        return background;
    }

    public void setBackground(MultipartFile background) {
        this.background = background;
    }

    @Override
    public String toString() {
        return "NDAView [logo=" + logo + ", title=" + title + ", background=" + background + "]";
    }

}
