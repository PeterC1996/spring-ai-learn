package dev.learn.tools.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CalculatorTest {

    @Test
    void addsAndMultiplies() {
        assertThat(Calculator.calculate(1.5, "+", 2.5)).isEqualTo("4");
        assertThat(Calculator.calculate(12, "*", 7)).isEqualTo("84");
    }

    @Test
    void rejectsDivisionByZeroAndUnknownOperator() {
        assertThat(Calculator.calculate(1, "/", 0)).contains("不能为 0");
        assertThat(Calculator.calculate(1, "^", 2)).contains("不支持");
    }
}
