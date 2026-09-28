package dev.learn.tools.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import dev.learn.tools.support.Calculator;

@Component
public class CalculatorTools {

    @Tool(description = "计算两个数字的四则运算。用户要求加、减、乘、除时调用，不要心算。")
    public String calculate(
            @ToolParam(description = "左操作数") double left,
            @ToolParam(description = "运算符，只能是 +、-、*、/") String operator,
            @ToolParam(description = "右操作数") double right) {
        return Calculator.calculate(left, operator, right);
    }
}
