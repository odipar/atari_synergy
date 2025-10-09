package org.atari.synergy;

/*
  Non-exact multiplication and division using a logarithm table and exponent table, bound by a short/integer of size S.
  Typically S is 8192 (14 * 14 ~ 28 bits accuracy) or 4096 to keep table sizes relatively small (halve a megabyte).
  
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
   a: the negative offset -S*2 into the exponent table encodes i*pi
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
        var l1 = log(100);
        var l2 = log(0);
        var result = exp(l1 - l2);
        System.out.println("result: " + result);
        System.out.println("table size in bytes: " + total_mem_bytes);
        System.out.println("% of megabyte: " + megabyte_p);
        
    }
    
    public static int log(int value) {
        return signed_log[log_offset + value];
    }
    
    public static int exp(int signed_log) {
        return signed_exp[exp_offset + signed_log];
    }
    
    private static final int S = 8192;
    private static final int d_S = S * 2;
    private static final int log_offset = S - 1;
    private static final int neg_offset = -d_S;
    private static final int zero_offset = d_S * 2;
    private static final int exp_offset = zero_offset;
    private static final double factor = S / Math.log(S);
    
    private static final int[] log = new int[S];
    private static final int[] exp = new int[d_S];
    private static final int[] signed_log = new int[d_S];
    private static final int[] signed_exp = new int[d_S * 6 + 1];
    
    private static final int total_mem_bytes = (d_S * 6 + 1 + d_S + d_S + S) * 4;
    private static final int megabyte_p = (100 * total_mem_bytes) / (1024 * 1024);
    
    static {
        init_tables();
    }
    
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
        for (int i = 1; i < S; i++) { signed_log[l++] = log[S - i] + neg_offset; }
        signed_log[l++] = zero_offset;
        for (int i = 1; i < S; i++) { signed_log[l++] = log[i]; }
    }
    
    public static void init_signed_exp_table() {
        var e = 0;
        for (int i = 0; i < d_S; i++) { signed_exp[e++] = exp[i]; }  // - * -
        for (int i = 0; i < d_S; i++) { signed_exp[e++] = -exp[i]; } // - * + || + * -
        for (int i = 0; i < d_S; i++) { signed_exp[e++] = exp[i]; }  // + * +
        for (int i = 0; i < d_S * 2; i++) { signed_exp[e++] = 0; }   // - * 0, 0 * -, + * 0, 0 * +
        signed_exp[e] = 0;                                           // 0 * 0
    }
}