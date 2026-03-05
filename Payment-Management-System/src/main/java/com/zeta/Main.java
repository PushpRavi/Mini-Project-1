package com.zeta;

import com.zeta.config.ApplicationConfig;
import com.zeta.view.ConsoleRouter;

public class Main {
    public static void main(String[] args) {

        ApplicationConfig config = new ApplicationConfig();

        ConsoleRouter router = new ConsoleRouter(
                config.getUserService(),
                config.getPaymentService(),
                config.getAuditService()
        );
        router.start();
    }

}