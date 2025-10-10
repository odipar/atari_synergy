package org.atari.synergy;

import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;
import static org.atari.synergy.SignedFixedPointLogExpTable.*;

public class LogExpTest {
    static final double avg_error_margin    = 0.0004;
    static final double single_error_margin = 0.001;
    
    static final Percentage error_p = Percentage.withPercentage(single_error_margin * 100.0);
    
    public static int mul(int a, int b) {
        return exp(log(a) + log(b));
    }
    
    @Test
    public void mul_log_exp() {
        assertThat(mul(10, 10)).isEqualTo(10 * 10);
        assertThat(mul(-10, 10)).isEqualTo(-10 * 10);
        assertThat(mul(10, -10)).isEqualTo(10 * -10);
        assertThat(mul(-10, -10)).isEqualTo(-10 * -10);
        
        assertThat(mul(8000, 8000)).isCloseTo(8000 * 8000, error_p);
        assertThat(mul(-8000, 8000)).isCloseTo(-8000 * 8000, error_p);
        assertThat(mul(8000, -8000)).isCloseTo(8000 * -8000, error_p);
        assertThat(mul(-8000, -8000)).isCloseTo(-8000 * -8000, error_p);
        
        assertThat(mul(0, 8000)).isEqualTo(0);
        assertThat(mul(8000, 0)).isEqualTo(0);
        assertThat(mul(0, -8000)).isEqualTo(0);
        assertThat(mul(-8000, 0)).isEqualTo(0);
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
        double avg_error = sum_error / n;
        assertThat(avg_error).isLessThan(avg_error_margin);
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
        double avg_error = sum_error / n;
        assertThat(avg_error).isLessThan(avg_error_margin);
    }
    
}
