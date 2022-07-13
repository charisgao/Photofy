package com.example.photofy.models;

public class Welcome {

    private int image;
    private String mainText;
    private String descriptionText;

    public Welcome(int image, String mainText, String descriptionText) {
        this.image = image;
        this.mainText = mainText;
        this.descriptionText = descriptionText;
    }

    public int getImage() {
        return image;
    }

    public String getMainText() {
        return mainText;
    }

    public String getDescriptionText() {
        return descriptionText;
    }
}
