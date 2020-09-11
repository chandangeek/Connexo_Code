/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl.importers.csr;

import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.pki.CaService;
import com.elster.jupiter.pki.CertificateWrapperStatus;
import com.elster.jupiter.pki.RequestableCertificateWrapper;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.TrustedCertificate;
import com.elster.jupiter.pki.impl.MessageSeeds;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.conditions.Condition;

import com.google.common.collect.ImmutableMap;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.assertj.core.data.MapEntry;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CSRProcessorTest {
    private static final String PRESENT_SERIAL = "ABC";
    private static final String PRESENT_NAME = "654";
    private static final String ABSENT_NAME = "123";
    private static final String PRESENT_ALIAS = PRESENT_SERIAL + '-' + PRESENT_NAME;
    private static final String ABSENT_ALIAS = PRESENT_SERIAL + '-' + ABSENT_NAME;
    private static final TimeDuration TIMEOUT = TimeDuration.seconds(1);
    private static final Map<String, Object> properties =  new HashMap<>();
    @Mock
    private SecurityManagementService securityManagementService;
    @Mock
    private CaService caService;
    @Mock
    private CSRImporterLogger logger;
    @Mock
    private PKCS10CertificationRequest csr1, csr2;
    @Mock
    private X509Certificate cert1, cert2;
    @Mock
    private RequestableCertificateWrapper wrapper, newWrapper;
    private CSRProcessor processor;
    private Map<String, Map<String, PKCS10CertificationRequest>> csrMap = new HashMap<>();

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void setUp() {
        properties.put(CSRImporterTranslatedProperty.TIMEOUT.getPropertyKey(), TIMEOUT);
        properties.put(CSRImporterTranslatedProperty.SAVE_CERTIFICATE.getPropertyKey(), true);
        when(logger.getThesaurus()).thenReturn(NlsModule.FakeThesaurus.INSTANCE);
        when(securityManagementService.findCertificateWrapper(anyString())).thenReturn(Optional.empty());
        when(securityManagementService.findCertificateWrapper(PRESENT_ALIAS)).thenReturn(Optional.of(wrapper));
        when(securityManagementService.isUsedByCertificateAccessors(wrapper)).thenReturn(false);
        when(securityManagementService.streamDirectoryCertificateUsages()).thenReturn(FakeBuilder.initBuilderStub(false, QueryStream.class));
        when(securityManagementService.getCertificateAssociatedDevicesNames(wrapper)).thenReturn(Collections.emptyList());
        when(wrapper.getAlias()).thenReturn(PRESENT_ALIAS);
        when(securityManagementService.newCertificateWrapper(anyString())).thenAnswer(invocationOnMock -> {
            String newAlias = invocationOnMock.getArgumentAt(0, String.class);
            when(newWrapper.getAlias()).thenReturn(newAlias);
            when(securityManagementService.findCertificateWrapper(newAlias)).thenReturn(Optional.of(newWrapper));
            return newWrapper;
        });
        when(caService.signCsr(csr1, Optional.empty())).thenReturn(cert1);
        when(caService.signCsr(csr2, Optional.empty())).thenReturn(cert2);
        processor = new CSRProcessor(securityManagementService, caService, properties, logger, Optional.empty());
    }

    @After
    public void reset() {
        csrMap.clear();
    }

    private void addCSRToMap(String serial, String name, PKCS10CertificationRequest csr) {
        csrMap.computeIfAbsent(serial, key -> new HashMap<>())
                .put(name, csr);
    }

    @Test
    public void testProcessCSRSuccessfully() {
        addCSRToMap(PRESENT_SERIAL, ABSENT_NAME + "-123", csr1);
        addCSRToMap(PRESENT_SERIAL, PRESENT_NAME + "-123", csr2);

        assertThat(processor.process(csrMap)).containsOnly(MapEntry.entry(PRESENT_SERIAL, ImmutableMap.of(
                ABSENT_NAME + "-123", cert1,
                PRESENT_NAME + "-123", cert2
        )));
        verify(newWrapper).setCSR(eq(csr1), any(), any());
        verify(newWrapper).setCertificate(cert1, Optional.empty());
        verify(newWrapper).setWrapperStatus(CertificateWrapperStatus.NATIVE);
        verify(newWrapper, atLeast(1)).save();
        verify(logger).log(MessageSeeds.CSR_IMPORTED_SUCCESSFULLY, ABSENT_ALIAS);
        verify(logger).log(MessageSeeds.CSR_SIGNED_SUCCESSFULLY, ABSENT_ALIAS);
        verify(logger).log(MessageSeeds.CERTIFICATE_IMPORTED_SUCCESSFULLY, ABSENT_ALIAS);
        verify(wrapper).setCSR(eq(csr2), any(), any());
        verify(wrapper).setCertificate(cert2, Optional.empty());
        verify(wrapper).setWrapperStatus(CertificateWrapperStatus.NATIVE);
        verify(wrapper, atLeast(1)).save();
        verify(logger).log(MessageSeeds.CSR_IMPORTED_SUCCESSFULLY, PRESENT_ALIAS);
        verify(logger).log(MessageSeeds.CSR_SIGNED_SUCCESSFULLY, PRESENT_ALIAS);
        verify(logger).log(MessageSeeds.CERTIFICATE_IMPORTED_SUCCESSFULLY, PRESENT_ALIAS);
    }

    @Test
    public void testSigningFailed() {
        addCSRToMap(PRESENT_SERIAL, PRESENT_NAME + "-123", csr2);
        when(caService.signCsr(csr2, Optional.empty())).thenThrow(new IllegalArgumentException("Can't sign CSR2."));

        exceptionRule.expect(CSRImporterException.class);
        exceptionRule.expectMessage("Certificate signing request to CA has failed for alias " + PRESENT_ALIAS + ": Can't sign CSR2.");
        processor.process(csrMap);
    }

    @Test
    public void testSigningTimedOut() {
        addCSRToMap(PRESENT_SERIAL, PRESENT_NAME + "-123", csr2);
        when(caService.signCsr(csr2, Optional.empty())).thenAnswer(invocationOnMock -> {
            Thread.sleep(TIMEOUT.getMilliSeconds() + 100);
            return cert2;
        });

        exceptionRule.expect(CSRImporterException.class);
        exceptionRule.expectMessage("Certificate signing request to CA has timed out for alias " + PRESENT_ALIAS + ". The certificate isn't signed.");
        processor.process(csrMap);
    }

    @Test
    public void testSomethingGoesWrong() {
        addCSRToMap(PRESENT_SERIAL, PRESENT_NAME + "-123", csr2);
        TrustedCertificate trustedCertificate = mock(TrustedCertificate.class);
        when(securityManagementService.findCertificateWrapper(PRESENT_ALIAS)).thenReturn(Optional.of(trustedCertificate));

        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("For some reason trusted certificate is found instead of a requestable one.");
        processor.process(csrMap);
    }

    @Test
    public void testCertificateIsInUseByCentrallyManagedSecurityAccessor() {
        addCSRToMap(PRESENT_SERIAL, PRESENT_NAME + "-123", csr2);
        when(securityManagementService.isUsedByCertificateAccessors(wrapper)).thenReturn(true);

        exceptionRule.expect(CSRImporterException.class);
        exceptionRule.expectMessage("Can't import CSR for certificate with alias " + PRESENT_ALIAS + ": it is currently in use.");
        processor.process(csrMap);
    }

    @Test
    public void testCertificateIsInUseByUserDirectory() {
        addCSRToMap(PRESENT_SERIAL, PRESENT_NAME + "-123", csr2);
        QueryStream queryStream = mock(QueryStream.class);
        when(securityManagementService.streamDirectoryCertificateUsages()).thenReturn(queryStream);
        when(queryStream.anyMatch(any(Condition.class))).thenReturn(true);

        exceptionRule.expect(CSRImporterException.class);
        exceptionRule.expectMessage("Can't import CSR for certificate with alias " + PRESENT_ALIAS + ": it is currently in use.");
        processor.process(csrMap);
    }

    @Test
    public void testCertificateIsInUseByDeviceSecurityAccessor() {
        addCSRToMap(PRESENT_SERIAL, PRESENT_NAME + "-123", csr2);
        when(securityManagementService.getCertificateAssociatedDevicesNames(wrapper)).thenReturn(Collections.singletonList("Something"));

        exceptionRule.expect(CSRImporterException.class);
        exceptionRule.expectMessage("Can't import CSR for certificate with alias " + PRESENT_ALIAS + ": it is currently in use.");
        processor.process(csrMap);
    }

    @Test
    public void testWrongFileNameFormat1() {
        addCSRToMap(PRESENT_SERIAL, ABSENT_NAME, csr1);

        exceptionRule.expect(CSRImporterException.class);
        exceptionRule.expectMessage("Unexpected file name format in the imported zip. File name should contain non-empty file prefix and file system separated with a hyphen.");
        processor.process(csrMap);
    }

    @Test
    public void testWrongFileNameFormat2() {
        addCSRToMap(PRESENT_SERIAL, ABSENT_NAME + '-', csr1);

        exceptionRule.expect(CSRImporterException.class);
        exceptionRule.expectMessage("Unexpected file name format in the imported zip. File name should contain non-empty file prefix and file system separated with a hyphen.");
        processor.process(csrMap);
    }

    @Test
    public void testWrongFileNameFormat3() {
        addCSRToMap(PRESENT_SERIAL, '-' + ABSENT_NAME, csr1);

        exceptionRule.expect(CSRImporterException.class);
        exceptionRule.expectMessage("Unexpected file name format in the imported zip. File name should contain non-empty file prefix and file system separated with a hyphen.");
        processor.process(csrMap);
    }

    @Test
    public void testFileNameFormatIsOk() {
        addCSRToMap("CDE", "123-123-123-123", csr1);

        assertThat(processor.process(csrMap)).containsOnly(MapEntry.entry("CDE", ImmutableMap.of(
                "123-123-123-123", cert1
        )));
        verify(newWrapper).setCSR(eq(csr1), any(), any());
        verify(newWrapper).setCertificate(cert1, Optional.empty());
        verify(newWrapper).setWrapperStatus(CertificateWrapperStatus.NATIVE);
        verify(newWrapper, atLeast(1)).save();
        verify(logger).log(MessageSeeds.CSR_IMPORTED_SUCCESSFULLY, "CDE-123-123-123");
        verify(logger).log(MessageSeeds.CSR_SIGNED_SUCCESSFULLY, "CDE-123-123-123");
        verify(logger).log(MessageSeeds.CERTIFICATE_IMPORTED_SUCCESSFULLY, "CDE-123-123-123");
    }
}
