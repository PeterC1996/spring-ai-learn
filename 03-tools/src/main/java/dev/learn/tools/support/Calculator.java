package dev.learn.tools.support;

/**
 * 只做两个数的四则运算，避免把任意字符串交给脚本引擎。
 */
public final class Calculator {

    private Calculator() {
    }

    public static String calculate(double left, String operator, double right) {
        if (operator == null || operator.isBlank()) {
            return "运算符不能为空，请使用 + - * /";
        }
        String op = operator.trim();
        return switch (op) {
            case "+" -> format(left + right);
            case "-" -> format(left - right);
            case "*", "×" -> format(left * right);
            case "/", "÷" -> right == 0.0d ? "除数不能为 0" : format(left / right);
            default -> "不支持的运算符: " + op + "。请使用 + - * /";
        };
    }

    private static String format(double value) {
        if (Double.isInfinite(value) || Double.isNaN(value)) {
            return "结果无效";
        }
        if (value == Math.rint(value) && Math.abs(value) < Long.MAX_VALUE) {
            return Long.toString((long) value);
        }
        return Double.toString(value);
    }
}
