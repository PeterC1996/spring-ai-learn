package dev.learn.tools.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderCatalogTest {

    @Test
    void findsKnownOrderIgnoringCase() {
        assertThat(OrderCatalog.lookup("a1001")).contains("已发货").contains("SF10001");
    }

    @Test
    void unknownOrderListsSamples() {
        assertThat(OrderCatalog.lookup("ZZZ")).contains("没有找到").contains("A1002");
    }
}
