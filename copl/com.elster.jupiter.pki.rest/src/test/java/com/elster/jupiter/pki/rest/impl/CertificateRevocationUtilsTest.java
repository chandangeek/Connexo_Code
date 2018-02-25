package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.pki.CaService;
import com.elster.jupiter.pki.CertificateAuthoritySearchFilter;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.CertificateWrapperStatus;
import com.elster.jupiter.pki.RevokeStatus;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CertificateRevocationUtilsTest {
    //Keep alphabetical order for objects properties
    private static final String ALIAS_1 = "alias_1";
    private static final String ALIAS_2 = "alias_2";
    private static final Long ID_1 = 1L;
    private static final Long ID_2 = 2L;
    private static final BigInteger SERIAL_1 = BigInteger.valueOf(111);
    private static final BigInteger SERIAL_2 = BigInteger.valueOf(112);
    private static final String ISSUER_1 = "issuer_1";
    private static final String ISSUER_2 = "issuer_2";
    private static final String SUBJECT_1 = "subject_1";
    private static final String SUBJECT_2 = "subject_2";
    private static final Long TIMEOUT = 10000L;
    private static final Long SHORT_TIMEOUT = 100L;

    //Under test
    private CertificateRevocationUtils revocationUtils;

    @Mock
    private SecurityManagementService securityManagementService;
    @Mock
    private TransactionService transactionService;
    @Mock
    private TransactionContext transactionContext;
    @Mock
    private CaService caService;
    @Mock
    private NlsService nlsService;
    @Mock
    private CertificateWrapper wrapper1;
    @Mock
    private CertificateWrapper wrapper2;
    @Mock
    private X509Certificate x509Certificate1;
    @Mock
    private X509Certificate x509Certificate2;

    @Captor
    private ArgumentCaptor<CertificateAuthoritySearchFilter> revokeFilterCaptor;
    @Captor
    private ArgumentCaptor<CertificateAuthoritySearchFilter> checkFilterCaptor;

    @Before
    public void setup() {
        //common mocks
        when(nlsService.getThesaurus(PkiApplication.COMPONENT_NAME, Layer.REST)).thenReturn(NlsModule.FakeThesaurus.INSTANCE);
        when(transactionService.getContext()).thenReturn(transactionContext);
        when(wrapper1.getId()).thenReturn(ID_1);
        when(wrapper2.getId()).thenReturn(ID_2);
        when(wrapper1.getAlias()).thenReturn(ALIAS_1);
        when(wrapper2.getAlias()).thenReturn(ALIAS_2);
        when(wrapper1.getIssuer()).thenReturn(ISSUER_1);
        when(wrapper2.getIssuer()).thenReturn(ISSUER_2);
        when(wrapper1.getSubject()).thenReturn(SUBJECT_1);
        when(wrapper2.getSubject()).thenReturn(SUBJECT_2);
        when(wrapper1.getCertificate()).thenReturn(Optional.of(x509Certificate1));
        when(wrapper2.getCertificate()).thenReturn(Optional.of(x509Certificate2));
        when(x509Certificate1.getSerialNumber()).thenReturn(SERIAL_1);
        when(x509Certificate2.getSerialNumber()).thenReturn(SERIAL_2);

        revocationUtils = new CertificateRevocationUtils(securityManagementService, caService, nlsService, transactionService);
    }

    @Test
    public void testIsCAConfigured() throws Exception {
        //Prepare
        when(caService.isConfigured()).thenReturn(true).thenReturn(false);

        //Act and verify
        assertThat(revocationUtils.isCAConfigured()).isEqualTo(true);
        assertThat(revocationUtils.isCAConfigured()).isEqualTo(false);
    }

    @Test
    public void testFindAllCertificateWrappers() throws Exception {
        //Prepare
        when(securityManagementService.findCertificateWrapper(ID_1)).thenReturn(Optional.of(wrapper1));
        when(securityManagementService.findCertificateWrapper(ID_2)).thenReturn(Optional.of(wrapper2));

        //Act
        List<CertificateWrapper> certs = revocationUtils.findAllCertificateWrappers(Arrays.asList(ID_1, ID_2));

        //Verify
        assertThat(certs).hasSize(2).contains(wrapper1, wrapper2);
    }

    @Test
    public void testFindAllCertificateWrappers_missing() throws Exception {
        //Prepare
        when(securityManagementService.findCertificateWrapper(ID_1)).thenReturn(Optional.of(wrapper1));
        when(securityManagementService.findCertificateWrapper(ID_2)).thenReturn(Optional.empty());

        //Act and verify
        try {
            revocationUtils.findAllCertificateWrappers(Arrays.asList(ID_1, ID_2));
            failNoException();
        } catch (ExceptionFactory.RestException e) {
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.NO_CERTIFICATES_WITH_IDS);
        }
    }

    @Test
    public void testRevokeCertificate_caIsConfigured() throws Exception {
        //Prepare
        when(caService.isConfigured()).thenReturn(true);
        when(caService.checkRevocationStatus(any(CertificateAuthoritySearchFilter.class)))
                .thenReturn(RevokeStatus.NOT_REVOKED)
                .thenReturn(RevokeStatus.REVOCATION_REASON_UNSPECIFIED);

        //Act
        revocationUtils.revokeCertificate(wrapper1, TIMEOUT);

        //Verify
        verify(caService, times(1)).revokeCertificate(revokeFilterCaptor.capture(), eq(RevokeStatus.REVOCATION_REASON_UNSPECIFIED.getVal()));
        verify(caService, times(2)).checkRevocationStatus(checkFilterCaptor.capture());
        verify(wrapper1, times(1)).setWrapperStatus(CertificateWrapperStatus.REVOKED);
        verify(wrapper1, times(1)).save();

        CertificateAuthoritySearchFilter revokeFilter = revokeFilterCaptor.getValue();
        CertificateAuthoritySearchFilter checkFilter = checkFilterCaptor.getValue();
        assertThat(revokeFilter).isSameAs(checkFilter);
        assertThat(revokeFilter.getIssuerDN()).isEqualTo(ISSUER_1);
        assertThat(revokeFilter.getSubjectDN()).isEqualTo(SUBJECT_1);
        assertThat(revokeFilter.getSerialNumber()).isEqualTo(SERIAL_1);
    }

    @Test
    public void testRevokeCertificate_caIsConfigured_alreadyRevoked() throws Exception {
        //Prepare
        when(caService.isConfigured()).thenReturn(true);
        when(caService.checkRevocationStatus(any(CertificateAuthoritySearchFilter.class)))
                .thenReturn(RevokeStatus.REVOCATION_REASON_UNSPECIFIED);

        //Act
        revocationUtils.revokeCertificate(wrapper1, TIMEOUT);

        //Verify
        verify(caService, times(1)).checkRevocationStatus(checkFilterCaptor.capture());
        verify(caService, never()).revokeCertificate(any(CertificateAuthoritySearchFilter.class), eq(RevokeStatus.REVOCATION_REASON_UNSPECIFIED.getVal()));
        verify(wrapper1, times(1)).setWrapperStatus(CertificateWrapperStatus.REVOKED);
        verify(wrapper1, times(1)).save();

        assertThat(checkFilterCaptor.getValue().getIssuerDN()).isEqualTo(ISSUER_1);
        assertThat(checkFilterCaptor.getValue().getSubjectDN()).isEqualTo(SUBJECT_1);
        assertThat(checkFilterCaptor.getValue().getSerialNumber()).isEqualTo(SERIAL_1);
    }

    @Test
    public void testRevokeCertificate_caIsConfigured_desync() throws Exception {
        //Prepare
        when(caService.isConfigured()).thenReturn(true);
        when(caService.checkRevocationStatus(any(CertificateAuthoritySearchFilter.class)))
                .thenReturn(RevokeStatus.NOT_REVOKED)
                .thenReturn(RevokeStatus.REVOCATION_REASON_UNSPECIFIED);
        doThrow(new RuntimeException("Nope, no status here")).when(wrapper1).save();

        //Act and verify
        try {
            revocationUtils.revokeCertificate(wrapper1, TIMEOUT);
            failNoException();
        } catch (ExceptionFactory.RestException e) {
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.REVOCATION_FAILED);
        }

        verify(caService, times(1)).revokeCertificate(any(CertificateAuthoritySearchFilter.class), anyInt());
        verify(caService, times(2)).checkRevocationStatus(any(CertificateAuthoritySearchFilter.class));
        verify(wrapper1, times(1)).setWrapperStatus(CertificateWrapperStatus.REVOKED);
        verify(wrapper1, times(1)).save();
    }

    @Test
    public void testRevokeCertificate_caIsConfigured_timeout() throws Exception {
        //Prepare
        when(caService.isConfigured()).thenReturn(true);
        doAnswer(invocation -> {
            //imitate CA is stuck
            TimeUnit.SECONDS.sleep(SHORT_TIMEOUT * 2);
            return null;
        }).when(caService).checkRevocationStatus(any(CertificateAuthoritySearchFilter.class));

        //Act and verify
        try {
            revocationUtils.revokeCertificate(wrapper1, SHORT_TIMEOUT);
            failNoException();
        } catch (ExceptionFactory.RestException e) {
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.REVOCATION_FAILED);
        }

        verify(caService, times(1)).checkRevocationStatus(any(CertificateAuthoritySearchFilter.class));
        verify(caService, never()).revokeCertificate(any(CertificateAuthoritySearchFilter.class), anyInt());
        verify(wrapper1, never()).setWrapperStatus(CertificateWrapperStatus.REVOKED);
        verify(wrapper1, never()).save();
    }

    @Test
    public void testRevokeCertificate_caIsNotConfigured() throws Exception {
        //Prepare
        when(caService.isConfigured()).thenReturn(false);

        //Act
        revocationUtils.revokeCertificate(wrapper1, TIMEOUT);

        //Verify
        verify(caService, never()).revokeCertificate(any(CertificateAuthoritySearchFilter.class), anyInt());
        verify(wrapper1, times(1)).setWrapperStatus(CertificateWrapperStatus.REVOKED);
        verify(wrapper1, times(1)).save();
    }

    @Test
    public void testBulkRevokeCertificates_caIsConfigured() throws Exception {
        //Prepare
        when(caService.isConfigured()).thenReturn(true);
        when(caService.checkRevocationStatus(new CertificateAuthoritySearchFilter(SERIAL_1, ISSUER_1, SUBJECT_1)))
                .thenReturn(RevokeStatus.NOT_REVOKED)
                .thenReturn(RevokeStatus.REVOCATION_REASON_UNSPECIFIED);
        when(caService.checkRevocationStatus(new CertificateAuthoritySearchFilter(SERIAL_2, ISSUER_2, SUBJECT_2)))
                .thenReturn(RevokeStatus.NOT_REVOKED)
                .thenReturn(RevokeStatus.REVOCATION_REASON_UNSPECIFIED);

        //Act
        CertificateRevocationResultInfo result = revocationUtils.bulkRevokeCertificates(Arrays.asList(wrapper1, wrapper2), TIMEOUT);

        //Verify
        verify(caService, times(4)).checkRevocationStatus(checkFilterCaptor.capture());
        verify(caService, times(2)).revokeCertificate(revokeFilterCaptor.capture(), anyInt());
        verify(wrapper1, times(1)).setWrapperStatus(CertificateWrapperStatus.REVOKED);
        verify(wrapper1, times(1)).save();
        verify(wrapper2, times(1)).setWrapperStatus(CertificateWrapperStatus.REVOKED);
        verify(wrapper2, times(1)).save();

        List<CertificateAuthoritySearchFilter> revokeFilters = revokeFilterCaptor.getAllValues().stream()
                .sorted(Comparator.comparing(CertificateAuthoritySearchFilter::getSubjectDN))
                .collect(Collectors.toList());
        List<CertificateAuthoritySearchFilter> checkFilters = checkFilterCaptor.getAllValues().stream()
                .sorted(Comparator.comparing(CertificateAuthoritySearchFilter::getSubjectDN))
                .collect(Collectors.toList());

        assertThat(revokeFilters).hasSize(2);
        assertThat(checkFilters).hasSize(4);
        assertThat(revokeFilters.get(0)).isSameAs(checkFilters.get(0)).isSameAs(checkFilters.get(1));
        assertThat(revokeFilters.get(0).getSerialNumber()).isEqualTo(SERIAL_1);
        assertThat(revokeFilters.get(0).getSubjectDN()).isEqualTo(SUBJECT_1);
        assertThat(revokeFilters.get(0).getIssuerDN()).isEqualTo(ISSUER_1);
        assertThat(revokeFilters.get(1)).isSameAs(checkFilters.get(2)).isSameAs(checkFilters.get(3));
        assertThat(revokeFilters.get(1).getSerialNumber()).isEqualTo(SERIAL_2);
        assertThat(revokeFilters.get(1).getSubjectDN()).isEqualTo(SUBJECT_2);
        assertThat(revokeFilters.get(1).getIssuerDN()).isEqualTo(ISSUER_2);

        assertThat(result.revoked).hasSize(2);
        assertThat(result.withErrors).isEmpty();
        assertThat(result.withUsages).isEmpty();

        result.revoked.sort(Comparator.comparing(o -> o.name));

        assertThat(result.revoked.get(0).id).isEqualTo(ID_1);
        assertThat(result.revoked.get(0).name).isEqualTo(ALIAS_1);
        assertThat(result.revoked.get(1).id).isEqualTo(ID_2);
        assertThat(result.revoked.get(1).name).isEqualTo(ALIAS_2);
    }

    @Test
    public void testBulkRevokeCertificates_caIsConfigured_partialSuccess() throws Exception {
        //Prepare
        when(caService.isConfigured()).thenReturn(true);
        when(caService.checkRevocationStatus(new CertificateAuthoritySearchFilter(SERIAL_1, ISSUER_1, SUBJECT_1)))
                .thenReturn(RevokeStatus.NOT_REVOKED)
                .thenReturn(RevokeStatus.REVOCATION_REASON_UNSPECIFIED);
        when(caService.checkRevocationStatus(new CertificateAuthoritySearchFilter(SERIAL_2, ISSUER_2, SUBJECT_2)))
                .thenReturn(RevokeStatus.NOT_REVOKED);

        //Act
        CertificateRevocationResultInfo result = revocationUtils.bulkRevokeCertificates(Arrays.asList(wrapper1, wrapper2), TIMEOUT);

        //Verify
        verify(caService, times(2)).revokeCertificate(any(CertificateAuthoritySearchFilter.class), anyInt());
        verify(caService, times(4)).checkRevocationStatus(any(CertificateAuthoritySearchFilter.class));
        verify(wrapper1, times(1)).setWrapperStatus(CertificateWrapperStatus.REVOKED);
        verify(wrapper1, times(1)).save();
        verify(wrapper2, never()).setWrapperStatus(CertificateWrapperStatus.REVOKED);
        verify(wrapper2, never()).save();

        assertThat(result.revoked).hasSize(1);
        assertThat(result.withErrors).hasSize(1);
        assertThat(result.withUsages).isEmpty();
        assertThat(result.revoked.get(0).id).isEqualTo(ID_1);
        assertThat(result.revoked.get(0).name).isEqualTo(ALIAS_1);
        assertThat(result.withErrors.get(0).id).isEqualTo(ID_2);
        assertThat(result.withErrors.get(0).name).isEqualTo(ALIAS_2);
    }

    @Test
    public void testBulkRevokeCertificates_caIsConfigured_timeout() throws Exception {
        //Prepare
        when(caService.isConfigured()).thenReturn(true);
        when(caService.checkRevocationStatus(new CertificateAuthoritySearchFilter(SERIAL_1, ISSUER_1, SUBJECT_1)))
                .thenReturn(RevokeStatus.NOT_REVOKED);
        when(caService.checkRevocationStatus(new CertificateAuthoritySearchFilter(SERIAL_2, ISSUER_2, SUBJECT_2)))
                .thenReturn(RevokeStatus.NOT_REVOKED);
        doAnswer(invocation -> {
            //imitate CA is stuck
            TimeUnit.SECONDS.sleep(SHORT_TIMEOUT * 2);
            return null;
        }).when(caService).revokeCertificate(any(CertificateAuthoritySearchFilter.class), anyInt());

        //Act
        CertificateRevocationResultInfo result = revocationUtils.bulkRevokeCertificates(Arrays.asList(wrapper1, wrapper2), SHORT_TIMEOUT);

        //Verify
        verify(caService, times(2)).checkRevocationStatus(any(CertificateAuthoritySearchFilter.class));
        verify(caService, times(2)).revokeCertificate(any(CertificateAuthoritySearchFilter.class), anyInt());
        verify(wrapper1, never()).setWrapperStatus(CertificateWrapperStatus.REVOKED);
        verify(wrapper1, never()).save();
        verify(wrapper2, never()).setWrapperStatus(CertificateWrapperStatus.REVOKED);
        verify(wrapper2, never()).save();

        assertThat(result.revoked).isEmpty();
        assertThat(result.withErrors).hasSize(2);
        assertThat(result.withUsages).isEmpty();

        result.withErrors.sort(Comparator.comparing(o -> o.name));

        assertThat(result.withErrors.get(0).id).isEqualTo(ID_1);
        assertThat(result.withErrors.get(0).name).isEqualTo(ALIAS_1);
        assertThat(result.withErrors.get(1).id).isEqualTo(ID_2);
        assertThat(result.withErrors.get(1).name).isEqualTo(ALIAS_2);
    }

    @Test
    public void testBulkRevokeCertificates_caIsNotConfigured() throws Exception {
        //Prepare
        when(caService.isConfigured()).thenReturn(false);

        //Act
        CertificateRevocationResultInfo result = revocationUtils.bulkRevokeCertificates(Arrays.asList(wrapper1, wrapper2), TIMEOUT);

        //Verify
        verify(caService, never()).revokeCertificate(any(CertificateAuthoritySearchFilter.class), anyInt());
        verify(wrapper1, times(1)).setWrapperStatus(CertificateWrapperStatus.REVOKED);
        verify(wrapper1, times(1)).save();
        verify(wrapper2, times(1)).setWrapperStatus(CertificateWrapperStatus.REVOKED);
        verify(wrapper2, times(1)).save();

        assertThat(result.revoked).hasSize(2);
        assertThat(result.withErrors).isEmpty();
        assertThat(result.withUsages).isEmpty();

        result.revoked.sort(Comparator.comparing(o -> o.name));

        assertThat(result.revoked.get(0).id).isEqualTo(ID_1);
        assertThat(result.revoked.get(0).name).isEqualTo(ALIAS_1);
        assertThat(result.revoked.get(1).id).isEqualTo(ID_2);
        assertThat(result.revoked.get(1).name).isEqualTo(ALIAS_2);
    }

    private void failNoException() {
        fail("Expected exception was not thrown");
    }
}