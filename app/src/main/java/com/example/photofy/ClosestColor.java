package com.example.photofy;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.example.photofy.models.Photo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ClosestColor {

    public static final String TAG = "ClosestColor";

    public final Color blue = Color.valueOf(0,0,255); //A
    public final Color red = Color.valueOf(255,0,0); //B
    public final Color yellow = Color.valueOf(255,211,1); //C
    public final Color peach = Color.valueOf(255,192,132); //D
    public final Color pink = Color.valueOf(255,27,185); //E
    public final Color green = Color.valueOf(1,255,0); //F
    public final Color magenta = Color.valueOf(211,17,255); //G
    public final Color purple = Color.valueOf(132,132,255); //H
    public final Color lightpink = Color.valueOf(245,158,220); //I
    public final Color cyan = Color.valueOf(1,255,193); //J
    public final Color aqua = Color.valueOf(0,132,149); //K
    public final Color yellowgreen = Color.valueOf(204,204,51); //L
    public final Color brown = Color.valueOf(158,79,69); //M

    private HashMap<Color, String> colorToMood;
    private HashMap<String, String> moodToGenre;
    private Photo image;
    private Context context;

    public ClosestColor(Photo image, Context context) {
        this.image = image;
        this.context = context;

        // color to mood mapping source: https://www.pnas.org/doi/10.1073/pnas.1910704117#sec-1, https://www.ocf.berkeley.edu/~acowen/music.html#
        // if I have time later on, can expand to more colors through https://design-milk.com/color-coded-diaries-emotions-300-days/
        colorToMood = new HashMap<Color, String>() {{
            put(blue, "amusing");
            put(red, "annoyed");
            put(yellow, "anxious");
            put(peach, "beautiful");
            put (pink, "calm");
            put(green, "dreamy");
            put(magenta, "energizing");
            put(purple, "desirous");
            put(lightpink, "indignant");
            put(cyan, "joyful");
            put(aqua, "sad");
            put(yellowgreen, "scary");
            put(brown, "triumphant");
        }};

        // separate hash map to map to genre, possible stretch goal is to use other features besides color to determine genre
        moodToGenre = new HashMap<String, String>() {{
            put("amusing", "novelty");
            put("annoyed", "synthpop"); //hyperpop
            put("anxious", "industrial");
            put("beautiful", "classical");
            put("calm", "ambient");
            put("dreamy", "soul");
            put("energizing", "dance pop");
            put("desirous", "indie pop");
            put("indignant", "rock");
            put("joyful", "r&b");
            put("sad", "sad lo-fi");
            put("scary", "horror punk");
            put("triumphant", "epicore");
        }};
    }

    public Color getDominantColor() {
        int color = Color.parseColor(image.getColor());
        Color dominantColor = Color.valueOf(Color.red(color), Color.green(color), Color.blue(color));
        return dominantColor;
    }

    public Color getClosestColor(Color dominantColor) {
        HashMap<Color, Double> distances = new HashMap<>();
        for (Color color : colorToMood.keySet()) {
            double distance = (Math.sqrt(Math.pow((color.red()-dominantColor.red()), 2) + Math.pow((color.green()- dominantColor.green()), 2) + Math.pow((color.blue()- dominantColor.blue()), 2)));
            distances.put(color, distance);
        }
        Color closest = Collections.min(distances.entrySet(), Map.Entry.comparingByValue()).getKey();
        Log.i(TAG, closest.toString());
        return closest;
    }

    public String getMood(Color closestColor) {
        return colorToMood.get(closestColor);
    }

    public String getGenre(String mood) {
        return moodToGenre.get(mood);
    }
}
