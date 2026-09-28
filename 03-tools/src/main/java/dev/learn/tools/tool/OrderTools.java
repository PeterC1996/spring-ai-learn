package dev.learn.tools.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import dev.learn.tools.support.OrderCatalog;

@Component
public class OrderTools {

    @Tool(description = "按订单号查询模拟订单的商品和状态。用户提到订单、物流、是否发货时调用。没有订单号就不要猜。")
    public String lookupOrder(@ToolParam(description = "订单号，例如 A1001") String orderId) {
        return OrderCatalog.lookup(orderId);
    }
}
