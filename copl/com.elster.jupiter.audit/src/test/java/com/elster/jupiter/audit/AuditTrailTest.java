/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit;

import com.elster.jupiter.domain.util.Finder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AuditTrailTest extends BaseAuditTrailTest {

    @Test
    public void testGetAuditTrail() {
        Finder<AuditTrail> finder = auditService.getAuditTrail(auditService.newAuditTrailFilter());

//        assertThat(finder.stream().map(AuditTrail::getOperation).findFirst().get()).isEqualTo("");
    }
}
