package org.synergy.atari;

import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;
import static org.synergy.atari.SignedFixedPointLogExpTable.*;

public class LogExpTest {
    static final double avg_error_margin    = 4.0 / S;
    static final double single_error_margin = 8.0 / S;
    
    static final Percentage error_p = Percentage.withPercentage(single_error_margin * 100.0);
    
    public static int mul(int a, int b) {
        return exp(log(a) + log(b));
    }
    
    @Test
    public void mul_log_exp() {
        var small = S1 / 10;        // near the start
        
        assertThat(mul(small, small)).isCloseTo(small * small, error_p);
        assertThat(mul(-small, small)).isCloseTo(-small * small, error_p);
        assertThat(mul(small, -small)).isCloseTo(small * -small, error_p);
        assertThat(mul(-small, -small)).isCloseTo(-small * -small, error_p);
        
        var big = (S1 * 9) / 10;     // near the end
        
        assertThat(mul(big, big)).isCloseTo(big * big, error_p);
        assertThat(mul(-big, big)).isCloseTo(-big * big, error_p);
        assertThat(mul(big, -big)).isCloseTo(big * -big, error_p);
        assertThat(mul(-big, -big)).isCloseTo(-big * -big, error_p);
        
        assertThat(mul(0, big)).isEqualTo(0);
        assertThat(mul(big, 0)).isEqualTo(0);
        assertThat(mul(0, -big)).isEqualTo(0);
        assertThat(mul(-big, 0)).isEqualTo(0);
        assertThat(mul(0, 0)).isEqualTo(0);
    }
    
    @Test
    public void log_exp_error() {
        int n = 0;
        double sum_error = 0.0;
        for (int i = -S1; i < S1; i++) {
            var x_exact = i;
            var x_approx = (double) exp(log(i));
            if (x_exact > 0) {
                var abs_error = Math.abs((x_approx - x_exact) / x_exact);
                sum_error += abs_error;
                n += 1;
            }
        }
        if (n > 0) {
            double avg_error = sum_error / n;
            assertThat(avg_error).isLessThan(avg_error_margin);
        }
    }
    
    @Test
    public void signed_mul_error() {
        double sum_error = 0.0;
        int n = 0;
        
        for (int x = -S1; x < S1; x++) {
            for (int y = -S1; y < S1; y++) {
                var mul_exact = x * y;
                var mul_approx = (double) exp(log(x) + log(y));
                if (mul_exact > 0) {
                    var abs_error = Math.abs((mul_approx - mul_exact) / mul_exact);
                    sum_error += abs_error;
                    n += 1;
                }
            }
        }
        if (n > 0) {
            double avg_error = sum_error / n;
            assertThat(avg_error).isLessThan(avg_error_margin);
        }
    }
    
}
