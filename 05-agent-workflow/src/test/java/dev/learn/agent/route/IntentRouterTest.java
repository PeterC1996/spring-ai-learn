package dev.learn.agent.route;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IntentRouterTest {

    private final IntentRouter router = new IntentRouter();

    @Test
    void routesOrdersBeforeKnowledge() {
        assertThat(router.decide("帮我查一下订单 A1001").route()).isEqualTo(Route.DB);
        assertThat(router.decide("order status please").route()).isEqualTo(Route.DB);
        assertThat(router.decide("订单里提到的课程口令是什么").route()).isEqualTo(Route.DB);
    }

    @Test
    void routesCourseMaterialToKnowledge() {
        assertThat(router.decide("课程口令是什么").route()).isEqualTo(Route.KNOWLEDGE);
        assertThat(router.decide("Spring AI 的学习顺序").route()).isEqualTo(Route.KNOWLEDGE);
    }

    @Test
    void routesEverythingElseToChitchat() {
        assertThat(router.decide("今天心情不错").route()).isEqualTo(Route.CHITCHAT);
        assertThat(router.decide(null).route()).isEqualTo(Route.CHITCHAT);
        assertThat(router.decide("  ").route()).isEqualTo(Route.CHITCHAT);
    }
}
