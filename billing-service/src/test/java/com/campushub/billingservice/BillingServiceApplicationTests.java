package com.campushub.billingservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "server.port=0",
        "grpc.server.port=0"
})
class BillingServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
