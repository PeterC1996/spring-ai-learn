package dev.learn.agent.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import dev.learn.agent.order.OrderCatalog;

@Component
public class OrderTools {

    @Tool(description = "按订单号查询模拟订单的商品和物流状态。只有数据库分支会把这个工具交给模型。")
    public String lookupOrder(@ToolParam(description = "订单号，例如 A1001") String orderId) {
        return OrderCatalog.lookup(orderId);
    }
}
