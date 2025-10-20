package org.synergy.atari;

/*
  Non-exact multiplication and division using a logarithm table and exponent table, bound by a short/integer of size S.
  Typically S is 4096 (13 * 13 ~ 26 bits accuracy) to keep table sizes relatively small (up to quarter of a megabyte).
  
  Both tables act like functions to allow the following mathematical laws to be exploited:
   1) multiplication:   exp(log(a) + log(b)) = a * b
   2) division:         exp(log(a) - log(b)) = a / b
   
  Or stated otherwise:
   - Multiplication can be transformed to addition
   - Division can be transformed to subtraction
   
  Exceptions:
   a: the logarithm of -X yields a complex number X + i*pi
   b: the logarithm of zero yields -oo
   
  Encoding:
   a: the negative offset -2*S into the exponent table encodes i*pi
   b: the positive offset  4*S into the exponent table encodes -oo
   
  The encoding is designed for non-conditional multiplication and division of signed integers of size S,
  using a single logarithm and a single exponent table.
  
  Provenance of this concept:
   Code: Synergy Mega Demo: Having a period in 3D space - Dessert part
   Date: 1993
   Author: Robbert van Dalen - Rapido of Synergy
   
 */
public class SignedFixedPointLogExpTable {
    
    public static void main(String[] args) {
        var x =  S / 2;
        var y = -S / 4;
        var l1 = log(x);
        var l2 = log(y);
        var e1 = exp(l1);
        var e2 = exp(l2);
        var result = exp(l1 + l2);
        
        println("x: " + x);
        println("y: " + y);
        println("log(x): " + l1);
        println("log(y): " + l2);
        println("exp(log(x)): " + e1);
        println("exp(log(y)): " + e2);
        println("exact result: " + x * y);
        println("approx result: " + result);
        println("table size in bytes: " + total_mem_bytes);
        println("% of megabyte: " + megabyte_p);
        
        // square root test
        var z = exp(log(3000)/2);
        println("square root of 3000: " + z);
    }
    
    public static int log(int value) { return signed_log[log_offset + value]; }
    public static int exp(int signed_log) { return signed_exp[exp_offset + signed_log]; }
    public static void println(String s) { System.out.println(s); }
    
    public static final int S = 4096;
    public static final int S1 = S - 1;
    public static final int d_S = S * 2;
    public static final int log_offset = S1;
    public static final int neg_offset = -d_S;
    public static final int zero_offset = d_S * 2;
    public static final int exp_offset = zero_offset;
    public static final int total_mem_bytes = (d_S * 6 + 1 + d_S + d_S + S) * 4;
    public static final int megabyte_p = (100 * total_mem_bytes) / (1024 * 1024);
    public static final double factor = S1 / Math.log(S1);
    
    private static final int[] log = new int[S];
    private static final int[] exp = new int[d_S];
    private static final int[] signed_log = new int[d_S];
    private static final int[] signed_exp = new int[d_S * 6 + 1];
    
    static { init_tables(); }
    
    private static void init_tables() {
        init_log_table();
        init_exp_table();
        init_signed_log_table();
        init_signed_exp_table();
    }
    
    public static void init_log_table() {
        for (int l = 0; l < S; l++) { log[l] = (int) ((Math.log(l) * factor) + 0.5); }
    }
    
    public static void init_exp_table() {
        for (int e = 0; e < d_S; e++) { exp[e] = (int) (Math.exp(e / factor) + 0.5); }
    }
    
    public static void init_signed_log_table() {
        var l = 0;
        for (int i = 1; i < S; i++) { signed_log[l++] = log[S - i] + neg_offset; }  // -X ( X + -2*S == X + i*pi)
        signed_log[l++] = zero_offset;                                              // 0  ( 4*S == -oo)
        for (int i = 1; i < S; i++) { signed_log[l++] = log[i]; }                   // +X ( X )
    }
    
    public static void init_signed_exp_table() {
        var e = 0;
        for (int i = 0; i < d_S; i++) { signed_exp[e++] = exp[i]; }  // - * -
        for (int i = 0; i < d_S; i++) { signed_exp[e++] = -exp[i]; } // - * + || + * -
        for (int i = 0; i < d_S; i++) { signed_exp[e++] = exp[i]; }  // + * +
        for (int i = 0; i < d_S * 3; i++) { signed_exp[e++] = 0; }   // - * 0, 0 * -, + * 0, 0 * +
        signed_exp[e] = 0;                                           // 0 * 0
    }
}