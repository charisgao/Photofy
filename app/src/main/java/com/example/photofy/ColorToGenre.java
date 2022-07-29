package com.example.photofy;

import android.graphics.Color;
import android.util.Log;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Given the hex code of the dominant color from an image (determined from DetectProperties),
 * generates a music genre associated with the image through color-mood theory.
 *
 * Color-mood theory research articles:
 * https://www.pnas.org/doi/10.1073/pnas.1910704117#sec-1
 * https://www.ocf.berkeley.edu/~acowen/music.html#
 *
 * Derived the 13 colors and moods from the aforementioned resources
 * Mapped moods to music genre using
 * https://www.mindbodygreen.com/articles/13-emotions-you-feel-when-listening-to-music-according-to-science/
 */
public class ColorToGenre {

    public static final String TAG = "ColorToGenre";

    private final static Color BLUE = Color.valueOf(0,0,255); //A
    private final static Color RED = Color.valueOf(255,0,0); //B
    private final static Color YELLOW = Color.valueOf(255,211,1); //C
    private final static Color PEACH = Color.valueOf(255,192,132); //D
    private final static Color PINK = Color.valueOf(255,27,185); //E
    private final static Color GREEN = Color.valueOf(1,255,0); //F
    private final static Color MAGENTA = Color.valueOf(211,17,255); //G
    private final static Color PURPLE = Color.valueOf(132,132,255); //H
    private final static Color LIGHTPINK = Color.valueOf(245,158,220); //I
    private final static Color CYAN = Color.valueOf(1,255,193); //J
    private final static Color AQUA = Color.valueOf(0,132,149); //K
    private final static Color YELLOWGREEN = Color.valueOf(204,204,51); //L
    private final static Color BROWN = Color.valueOf(158,79,69); //M
    private final static Color BLACK = Color.valueOf(0,0,0);

    // if I have time later on, can expand to more colors through https://design-milk.com/color-coded-diaries-emotions-300-days/
    public final static Map<Color, String> COLOR_TO_MOOD = new HashMap<Color, String>() {{
        put(BLUE, "amusing");
        put(RED, "annoyed");
        put(YELLOW, "anxious");
        put(PEACH, "beautiful");
        put(PINK, "calm");
        put(PURPLE, "desirous");
        put(GREEN, "dreamy");
        put(MAGENTA, "energizing");
        put(BLACK, "fear");
        put(CYAN, "joyful");
        put(LIGHTPINK, "loving");
        put(AQUA, "sad");
        put(YELLOWGREEN, "scary");
        put(BROWN, "triumphant");
    }};

    public final static Map<String, String> MOOD_TO_GENRE = new HashMap<String, String>() {{
        put("amusing", "hip-hop");
        put("annoyed", "synth-pop");
        put("anxious", "industrial");
        put("beautiful", "classical");
        put("calm", "ambient");
        put("desirous", "indie-pop");
        put("dreamy", "soul");
        put("energizing", "party");
        put("fear", "metal");
        put("loving", "romance");
        put("joyful", "r-n-b");
        put("sad", "sad");
        put("scary", "grindcore");
        put("triumphant", "alternative");
    }};

    private Color getDominantColor(String imageColor) {
        int color = Color.parseColor(imageColor);
        return Color.valueOf(Color.red(color), Color.green(color), Color.blue(color));
    }

    // gets closest color from the 13 colors + black to the dominant color
    private Color getClosestColor(String imageColor) {
        Color dominantColor = getDominantColor(imageColor);
        Map<Color, Double> distances = new HashMap<>();
        for (Color color : COLOR_TO_MOOD.keySet()) {
            double distance = (Math.sqrt(Math.pow((color.red() - dominantColor.red()), 2) + Math.pow((color.green() - dominantColor.green()), 2) + Math.pow((color.blue() - dominantColor.blue()), 2)));
            distances.put(color, distance);
        }
        Color closest = Collections.min(distances.entrySet(), Map.Entry.comparingByValue()).getKey();
        Log.i(TAG, closest.toString());
        return closest;
    }

    private String getMood(String imageColor) {
        Color closestColor = getClosestColor(imageColor);
        return COLOR_TO_MOOD.get(closestColor);
    }

    public String findGenreFromColor(String imageColor) {
        String mood = getMood(imageColor);
        return MOOD_TO_GENRE.get(mood);
    }
}
