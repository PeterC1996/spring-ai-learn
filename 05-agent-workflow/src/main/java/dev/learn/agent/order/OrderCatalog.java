package dev.learn.agent.order;

import java.util.Locale;
import java.util.Map;

/**
 * 与 03-tools 相同的三笔假订单，模块之间不互相依赖。
 */
public final class OrderCatalog {

    private static final Map<String, String> ORDERS = Map.of(
            "A1001", "订单 A1001：机械键盘，状态已发货，运单 SF10001",
            "A1002", "订单 A1002：无线鼠标，状态待支付",
            "A1003", "订单 A1003：显示器支架，状态已签收");

    private OrderCatalog() {
    }

    public static String lookup(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            return "请提供订单号，例如 A1001";
        }
        String key = orderId.trim().toUpperCase(Locale.ROOT);
        return ORDERS.getOrDefault(key, "没有找到订单 " + key + "。可尝试 A1001、A1002、A1003");
    }
}
