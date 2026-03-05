package com.zeta.service;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
    TestUserServiceIntegration.class,
    TestPaymentServiceIntegration.class,
    TestPaymentServiceUnit.class,
    TestAuditServiceIntegration.class,
})
public class AllServiceIntegrationTests {
}
