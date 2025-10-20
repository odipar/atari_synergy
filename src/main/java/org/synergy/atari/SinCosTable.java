package org.synergy.atari;

public class SinCosTable {
    public final static int PERIOD = 2048;
    public final static int MASK = PERIOD - 1;
    public final static short MAGNITUDE = 8192;
    
    // sin(x) in high short, cos(x) in low short
    private final static int[] sin_cos_table = new int[PERIOD];

    // TODO: precompute table values
}
