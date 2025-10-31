package org.synergy.atari;

public class Screen {
    public static final int WIDTH = 320;
    public static final int HEIGHT = 200;
    public static final int PIXEL_COUNT = WIDTH * HEIGHT;
    public final long[] planes = new long[PIXEL_COUNT/16];
    public final int[] palette = new int[16];
}
