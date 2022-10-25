/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.pki.CertificateStatus;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.CertificateWrapperStatus;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.DirectoryCertificateUsage;
import com.elster.jupiter.pki.PrivateKeyWrapper;
import com.elster.jupiter.pki.RequestableCertificateWrapper;
import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.rest.impl.CertificateRevocationInfo;
import com.elster.jupiter.pki.rest.impl.CertificateRevocationResultInfo;
import com.jayway.jsonpath.JsonModel;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.pki.rest.impl.MessageSeeds.NO_CSR_PRESENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 3/23/17.
 */
public class CertificateWrapperResourceTest extends PkiApplicationTest {

    private CertificateFactory certificateFactory;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        Security.addProvider(new BouncyCastleProvider());
        certificateFactory = CertificateFactory.getInstance("X.509", "BC");
    }

    @Test
    public void testImportCertificate() throws Exception {
        RequestableCertificateWrapper certificateWrapper = mock(RequestableCertificateWrapper.class);
        when(securityManagementService.newCertificateWrapper(anyString())).thenReturn(certificateWrapper);

        String fileName = "myRootCA.cert";
        Form form = new Form();
        form.param("alias", "myCert");
        String path = new URI(getClass().getClassLoader().getResource(fileName).getFile()).getPath();
        File file = new File(path);
        MultiPart multiPart = new MultiPart();
        final FileDataBodyPart filePart = new FileDataBodyPart("file", file, MediaType.APPLICATION_OCTET_STREAM_TYPE);
        FormDataBodyPart deviceTypeBodyPart = new FormDataBodyPart();
        deviceTypeBodyPart.setName("alias");
        deviceTypeBodyPart.setValue(MediaType.APPLICATION_JSON_TYPE, "myCert");
        multiPart.bodyPart(filePart).bodyPart(deviceTypeBodyPart);

        Response response = target("/certificates").
                request(MediaType.TEXT_PLAIN).
                post(Entity.entity(multiPart, MediaType.MULTIPART_FORM_DATA_TYPE));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<X509Certificate> certificateArgumentCaptor = ArgumentCaptor.forClass(X509Certificate.class);
        verify(securityManagementService, times(1)).newCertificateWrapper(stringArgumentCaptor.capture());
        verify(certificateWrapper, times(1)).setCertificate(certificateArgumentCaptor.capture(), any(Optional.class));
        assertThat(stringArgumentCaptor.getValue()).isEqualTo("myCert");
        assertThat(certificateArgumentCaptor.getValue().getIssuerDN().getName()).contains("CN=MyRootCA");
    }

    @Test
    public void testGetCertificateWrapperWithCertificate() throws Exception {
        Clock clock = Clock.fixed(Instant.ofEpochMilli(1488240000000L), ZoneId.systemDefault());

        ClientCertificateWrapper certificateWrapper = mock(ClientCertificateWrapper.class);
        when(securityManagementService.findCertificateWrapper(12345)).thenReturn(Optional.of(certificateWrapper));
        when(certificateWrapper.getCertificateRequestData()).thenReturn(Optional.empty());
        when(certificateWrapper.getAlias()).thenReturn("root");
        when(certificateWrapper.getVersion()).thenReturn(135L);
        when(certificateWrapper.getCertificate()).thenReturn(Optional.of(loadCertificate("myRootCA.cert")));
        when(certificateWrapper.getCertificateStatus()).thenReturn(Optional.of(CertificateStatus.EXPIRED));
        when(certificateWrapper.getExpirationTime()).thenReturn(Optional.of(Instant.now(clock)));
        when(certificateWrapper.getAllKeyUsages()).thenReturn(Optional.of("A, B, C"));
        when(certificateWrapper.getCertificateRequestData()).thenReturn(Optional.empty());
        PrivateKeyWrapper privateKeyWrapper = mock(PrivateKeyWrapper.class);
        when(certificateWrapper.getPrivateKeyWrapper()).thenReturn(privateKeyWrapper);
        when(privateKeyWrapper.getKeyEncryptionMethod()).thenReturn("DataVault");


        Response response = target("/certificates/12345").request().get();
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<String>get("alias")).isEqualTo("root");
        assertThat(model.<Integer>get("version")).isEqualTo(135);
        assertThat(model.<String>get("keyEncryptionMethod")).isEqualTo("DataVault");
        assertThat(model.<Long>get("expirationDate")).isEqualTo(1488240000000L);
        assertThat(model.<String>get("type")).isEqualTo("A, B, C");
        assertThat(model.<String>get("issuer")).contains("C=BE", "ST=Vlaanderen", "L=Kortrijk", "O=Honeywell", "OU=SmartEnergy", "CN=MyRootCA");
        assertThat(model.<String>get("subject")).contains("C=BE", "ST=Vlaanderen", "L=Kortrijk", "O=Honeywell", "OU=SmartEnergy", "CN=MyRootCA");
        assertThat(model.<Integer>get("certificateVersion")).isEqualTo(1);
        assertThat(model.<String>get("serialNumber")).isEqualTo("0xAE2C4D6D2EA74373");
        assertThat(model.<Instant>get("notBefore")).isNotNull();
        assertThat(model.<Instant>get("notAfter")).isNotNull();
        assertThat(model.<String>get("signatureAlgorithm")).isEqualToIgnoringCase("SHA256withECDSA");
    }

    @Test
    public void testGetCertificateWrapperWithCertificateWithLargeSerialNumber() throws Exception {
        Clock clock = Clock.fixed(Instant.ofEpochMilli(1488240000000L), ZoneId.systemDefault());

        ClientCertificateWrapper certificateWrapper = mock(ClientCertificateWrapper.class);
        when(securityManagementService.findCertificateWrapper(12345)).thenReturn(Optional.of(certificateWrapper));
        when(certificateWrapper.getAlias()).thenReturn("root");
        when(certificateWrapper.getVersion()).thenReturn(135L);
        when(certificateWrapper.getCertificate()).thenReturn(Optional.of(loadCertificate("honeywell.com.cert")));
        when(certificateWrapper.getCertificateStatus()).thenReturn(Optional.of(CertificateStatus.AVAILABLE));
        when(certificateWrapper.getExpirationTime()).thenReturn(Optional.of(Instant.now(clock)));
        when(certificateWrapper.getAllKeyUsages()).thenReturn(Optional.of("A, B, C"));
        when(certificateWrapper.getCertificateRequestData()).thenReturn(Optional.empty());
        PrivateKeyWrapper privateKeyWrapper = mock(PrivateKeyWrapper.class);
        when(certificateWrapper.getPrivateKeyWrapper()).thenReturn(privateKeyWrapper);
        when(privateKeyWrapper.getKeyEncryptionMethod()).thenReturn("DataVault");


        Response response = target("/certificates/12345").request().get();
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<String>get("serialNumber")).isEqualTo("0x100219083091830912830918230187891739817238917918273918");
    }

    @Test
    public void testGetCNWithHtmlCharacters() throws Exception {
        Clock clock = Clock.fixed(Instant.ofEpochMilli(1488240000000L), ZoneId.systemDefault());

        ClientCertificateWrapper certificateWrapper = mock(ClientCertificateWrapper.class);
        when(securityManagementService.findCertificateWrapper(12345)).thenReturn(Optional.of(certificateWrapper));
        when(certificateWrapper.getAlias()).thenReturn("root");
        when(certificateWrapper.getVersion()).thenReturn(135L);
        when(certificateWrapper.getCertificate()).thenReturn(Optional.of(loadCertificate("fishy.cert.pem")));
        when(certificateWrapper.getCertificateStatus()).thenReturn(Optional.of(CertificateStatus.AVAILABLE));
        when(certificateWrapper.getExpirationTime()).thenReturn(Optional.of(Instant.now(clock)));
        when(certificateWrapper.getAllKeyUsages()).thenReturn(Optional.of("A, B, C"));
        when(certificateWrapper.getCertificateRequestData()).thenReturn(Optional.empty());
        PrivateKeyWrapper privateKeyWrapper = mock(PrivateKeyWrapper.class);
        when(certificateWrapper.getPrivateKeyWrapper()).thenReturn(privateKeyWrapper);
        when(privateKeyWrapper.getKeyEncryptionMethod()).thenReturn("DataVault");


        Response response = target("/certificates/12345").request().get();
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<String>get("issuer")).contains("CN=\"aha<br>aha<br>haha<br>slsls\"");
    }

    @Test
    public void testDownloadCertificate() throws Exception {
        ClientCertificateWrapper certificateWrapper = mock(ClientCertificateWrapper.class);
        when(securityManagementService.findCertificateWrapper(12345)).thenReturn(Optional.of(certificateWrapper));
        X509Certificate x509Certificate = loadCertificate("myRootCA.cert");
        when(certificateWrapper.getCertificate()).thenReturn(Optional.of(x509Certificate));
        when(certificateWrapper.getAlias()).thenReturn("downloadedRootCa");

        Response response = target("/certificates/12345/download/certificate").request().get();
        InputStream entity = (InputStream) response.getEntity();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(entity).hasSameContentAs(new ByteArrayInputStream(x509Certificate.getEncoded()));
    }

    @Test
    public void testDownloadCSRButThereIsNone() throws Exception {
        ClientCertificateWrapper certificateWrapper = mock(ClientCertificateWrapper.class);
        when(securityManagementService.findCertificateWrapper(12345)).thenReturn(Optional.of(certificateWrapper));
        when(certificateWrapper.getCertificate()).thenReturn(Optional.empty());
        when(certificateWrapper.getCSR()).thenReturn(Optional.empty());
        when(certificateWrapper.hasCSR()).thenReturn(false);
        when(certificateWrapper.getAlias()).thenReturn("downloadedRootCa");


        Response response = target("/certificates/12345/download/csr").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<String>get("message")).isEqualTo(NO_CSR_PRESENT.getDefaultFormat());
        assertThat(model.<String>get("error")).isEqualTo(NO_CSR_PRESENT.getKey());
    }

    @Test
    public void testMarkCertificateObsolete() throws Exception {
        //Prepare
        Long certId = 111L;
        ClientCertificateWrapper cert = mock(ClientCertificateWrapper.class);

        when(securityManagementService.findCertificateWrapper(certId)).thenReturn(Optional.of(cert));
        when(securityManagementService.getAssociatedCertificateAccessors(cert)).thenReturn(Collections.emptyList());
        when(securityManagementService.getCertificateAssociatedDevicesNames(cert)).thenReturn(Collections.emptyList());
        when(securityManagementService.streamDirectoryCertificateUsages())
                .thenReturn(FakeBuilder.initBuilderStub(Collections.emptyList(), QueryStream.class));

        //Act
        Response response = target("/certificates/" + certId + "/markObsolete").request().post(null);

        //Verify
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(cert, times(1)).setWrapperStatus(CertificateWrapperStatus.OBSOLETE);
    }

    @Test
    public void testMarkCertificateObsolete_usages() throws Exception {
        //Prepare
        String accessorName = "accessor_1";
        String deviceName1 = "device_1";
        String deviceName2 = "device_2";
        String deviceName3 = "device_3";
        String deviceName4 = "device_4";
        String dirUsageName1 = "dirUsage_1";
        String dirUsageName2 = "dirUsage_2";

        List<String> deviceNames = Arrays.asList(deviceName1, deviceName2, deviceName3, deviceName4);
        DirectoryCertificateUsage dirUsage1 = mock(DirectoryCertificateUsage.class);
        DirectoryCertificateUsage dirUsage2 = mock(DirectoryCertificateUsage.class);
        SecurityAccessor accessor = mock(SecurityAccessor.class);
        SecurityAccessorType accessorType = mock(SecurityAccessorType.class);

        Long certId = 111L;
        ClientCertificateWrapper cert = mock(ClientCertificateWrapper.class);

        when(securityManagementService.findCertificateWrapper(certId)).thenReturn(Optional.of(cert));
        when(securityManagementService.getAssociatedCertificateAccessors(cert)).thenReturn(Arrays.asList(accessor));
        when(securityManagementService.getCertificateAssociatedDevicesNames(cert)).thenReturn(deviceNames);
        when(securityManagementService.streamDirectoryCertificateUsages())
                .thenReturn(FakeBuilder.initBuilderStub(Arrays.asList(dirUsage1, dirUsage2), QueryStream.class));
        when(accessor.getSecurityAccessorType()).thenReturn(accessorType);
        when(accessorType.getName()).thenReturn(accessorName);
        when(dirUsage1.getDirectoryName()).thenReturn(dirUsageName1);
        when(dirUsage2.getDirectoryName()).thenReturn(dirUsageName2);

        //Act
        Response response = target("/certificates/" + certId + "/markObsolete").request().post(null);

        //Verify
        assertThat(response.getStatus()).isEqualTo(Response.Status.ACCEPTED.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<JSONArray>get("userDirectories")).hasSize(2).contains(dirUsageName1, dirUsageName2);
        assertThat(model.<JSONArray>get("devices")).hasSize(3).contains(deviceName1, deviceName2, deviceName3);
        assertThat(model.<JSONArray>get("securityAccessors")).hasSize(1).contains(accessorName);
        verify(cert, never()).setWrapperStatus(CertificateWrapperStatus.OBSOLETE);
    }

    @Test
    public void testForceMarkCertificateObsolete() throws Exception {
        //Prepare
        Long certId = 112L;
        ClientCertificateWrapper certificateWrapper = mock(ClientCertificateWrapper.class);
        when(securityManagementService.findCertificateWrapper(certId)).thenReturn(Optional.of(certificateWrapper));

        //Act
        Response response = target("/certificates/" + certId + "/forceMarkObsolete").request().post(null);

        //Verify
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(securityManagementService, times(1)).findCertificateWrapper(certId);
        verifyNoMoreInteractions(securityManagementService);
        verify(certificateWrapper, times(1)).setWrapperStatus(CertificateWrapperStatus.OBSOLETE);
    }

    @Test
    public void testUnmarkCertificateObsolete() throws Exception {
        //Prepare
        Long certId = 111L;
        ClientCertificateWrapper certificateWrapper = mock(ClientCertificateWrapper.class);
        when(securityManagementService.findCertificateWrapper(certId)).thenReturn(Optional.of(certificateWrapper));

        //Act
        Response response = target("/certificates/" + certId + "/unmarkObsolete").request().post(null);

        //Verify
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(certificateWrapper, times(1)).setWrapperStatus(CertificateWrapperStatus.NATIVE);
    }

    @Test
    public void testCheckRevokeCertificate() throws Exception {
        //Prepare
        Long certId = 222L;
        CertificateWrapper cert = mock(CertificateWrapper.class);

        when(securityManagementService.findCertificateWrapper(certId)).thenReturn(Optional.of(cert));
        when(securityManagementService.getAssociatedCertificateAccessors(cert)).thenReturn(Collections.emptyList());
        when(securityManagementService.getCertificateAssociatedDevicesNames(cert)).thenReturn(Collections.emptyList());
        when(securityManagementService.streamDirectoryCertificateUsages())
                .thenReturn(FakeBuilder.initBuilderStub(Collections.emptyList(), QueryStream.class));
        when(revocationUtils.isCAConfigured()).thenReturn(true);


        //Act
        Response response = target("/certificates/" + certId + "/checkRevoke").request().post(null);

        //Verify
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Boolean>get("isOnline")).isEqualTo(true);
    }


    @Test
    public void testCheckRevokeCertificate_usages() throws Exception {
        //Prepare
        String accessorName = "accessor";
        String deviceName = "device";
        String dirUsageName = "dirUsage";

        DirectoryCertificateUsage dirUsage = mock(DirectoryCertificateUsage.class);
        SecurityAccessor accessor = mock(SecurityAccessor.class);
        SecurityAccessorType accessorType = mock(SecurityAccessorType.class);

        Long certId = 222L;
        CertificateWrapper cert = mock(CertificateWrapper.class);

        when(securityManagementService.findCertificateWrapper(certId)).thenReturn(Optional.of(cert));
        when(securityManagementService.getAssociatedCertificateAccessors(cert)).thenReturn(Collections.singletonList(accessor));
        when(securityManagementService.getCertificateAssociatedDevicesNames(cert)).thenReturn(Collections.singletonList(deviceName));
        when(securityManagementService.streamDirectoryCertificateUsages())
                .thenReturn(FakeBuilder.initBuilderStub(Collections.singletonList(dirUsage), QueryStream.class));
        when(accessor.getSecurityAccessorType()).thenReturn(accessorType);
        when(accessorType.getName()).thenReturn(accessorName);
        when(dirUsage.getDirectoryName()).thenReturn(dirUsageName);


        //Act
        Response response = target("/certificates/" + certId + "/checkRevoke").request().post(null);

        //Verify
        assertThat(response.getStatus()).isEqualTo(Response.Status.ACCEPTED.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<JSONArray>get("userDirectories")).hasSize(1).contains(dirUsageName);
        assertThat(model.<JSONArray>get("devices")).hasSize(1).contains(deviceName);
        assertThat(model.<JSONArray>get("securityAccessors")).hasSize(1).contains(accessorName);
    }

    @Test
    public void testRevokeCertificate() throws Exception {
        //Prepare
        Long timeout = 10L;
        Long certId = 222L;
        CertificateWrapper cert = mock(CertificateWrapper.class);

        when(securityManagementService.findCertificateWrapper(certId)).thenReturn(Optional.of(cert));
        when(securityManagementService.getAssociatedCertificateAccessors(cert)).thenReturn(Collections.emptyList());
        when(securityManagementService.getCertificateAssociatedDevicesNames(cert)).thenReturn(Collections.emptyList());
        when(securityManagementService.streamDirectoryCertificateUsages())
                .thenReturn(FakeBuilder.initBuilderStub(Collections.emptyList(), QueryStream.class));

        //Act
        Response response = target("/certificates/" + certId + "/revoke").queryParam("timeout", timeout).request().post(null);

        //Verify
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(revocationUtils, times(1)).revokeCertificate(cert, timeout);
    }

    @Test
    public void testCheckBulkRevokeCertificate() throws Exception {
        //Prepare
        Long certId1 = 211L;
        Long certId2 = 212L;
        Long timeout = 10L;

        CertificateWrapper cert1 = mock(CertificateWrapper.class);
        CertificateWrapper cert2 = mock(CertificateWrapper.class);

        CertificateRevocationInfo requestInfo = new CertificateRevocationInfo();
        requestInfo.timeout = timeout;
        requestInfo.bulk.certificatesIds = Arrays.asList(certId1, certId2);

        when(cert1.getId()).thenReturn(certId1);
        when(cert2.getId()).thenReturn(certId2);
        when(cert1.getCertificateStatus()).thenReturn(Optional.of(CertificateStatus.AVAILABLE));
        when(cert2.getCertificateStatus()).thenReturn(Optional.of(CertificateStatus.AVAILABLE));
        when(revocationUtils.findAllCertificateWrappers(Arrays.asList(certId1, certId2))).thenReturn(Arrays.asList(cert1, cert2));
        when(revocationUtils.isCAConfigured()).thenReturn(true);
        when(securityManagementService.getAssociatedCertificateAccessors(cert1)).thenReturn(Collections.emptyList());
        when(securityManagementService.getAssociatedCertificateAccessors(cert2)).thenReturn(Collections.emptyList());
        when(securityManagementService.getCertificateAssociatedDevicesNames(cert1)).thenReturn(Collections.emptyList());
        when(securityManagementService.getCertificateAssociatedDevicesNames(cert2)).thenReturn(Collections.emptyList());
        when(securityManagementService.streamDirectoryCertificateUsages())
                .thenReturn(FakeBuilder.initBuilderStub(Collections.emptyList(), QueryStream.class));

        //Act
        Response response = target("/certificates/checkBulkRevoke").request().post(Entity.json(requestInfo));

        //Verify
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Boolean>get("isOnline")).isTrue();
        assertThat(model.<Integer>get("bulk.total")).isEqualTo(2);
        assertThat(model.<Integer>get("bulk.valid")).isEqualTo(2);
        assertThat(model.<Integer>get("bulk.invalid")).isEqualTo(0);
        assertThat(model.<JSONArray>get("bulk.withUsages")).isEmpty();
        assertThat(model.<JSONArray>get("bulk.withWrongStatus")).isEmpty();
        assertThat(model.<JSONArray>get("bulk.certificatesIds")).hasSize(2).contains(certId1.intValue(), certId2.intValue());
    }

    @Test
    public void testCheckBulkRevokeCertificate_usages() throws Exception {
        //Prepare
        Long certId1 = 211L;
        Long certId2 = 212L;
        Long certId3 = 213L;
        Long timeout = 10L;
        String accessorName = "accessor";
        String deviceName = "device";
        String dirUsageName = "directory";

        DirectoryCertificateUsage dirUsage = mock(DirectoryCertificateUsage.class);
        SecurityAccessor accessor = mock(SecurityAccessor.class);
        SecurityAccessorType accessorType = mock(SecurityAccessorType.class);
        CertificateWrapper cert1 = mock(CertificateWrapper.class);
        CertificateWrapper cert2 = mock(CertificateWrapper.class);
        CertificateWrapper cert3 = mock(CertificateWrapper.class);

        CertificateRevocationInfo requestInfo = new CertificateRevocationInfo();
        requestInfo.timeout = timeout;
        requestInfo.bulk.certificatesIds = Arrays.asList(certId1, certId2, certId3);

        when(cert1.getId()).thenReturn(certId1);
        when(cert2.getId()).thenReturn(certId2);
        when(cert3.getId()).thenReturn(certId3);
        when(revocationUtils.findAllCertificateWrappers(Arrays.asList(certId1, certId2, certId3))).thenReturn(Arrays.asList(cert1, cert2, cert3));
        when(revocationUtils.isCAConfigured()).thenReturn(true);
        when(cert1.getCertificateStatus()).thenReturn(Optional.of(CertificateStatus.AVAILABLE));
        when(cert2.getCertificateStatus()).thenReturn(Optional.of(CertificateStatus.AVAILABLE));
        when(cert3.getCertificateStatus()).thenReturn(Optional.of(CertificateStatus.REVOKED));
        when(securityManagementService.getAssociatedCertificateAccessors(cert1)).thenReturn(Collections.emptyList());
        when(securityManagementService.getAssociatedCertificateAccessors(cert2)).thenReturn(Collections.singletonList(accessor));
        when(securityManagementService.getAssociatedCertificateAccessors(cert3)).thenReturn(Collections.emptyList());
        when(securityManagementService.getCertificateAssociatedDevicesNames(cert1)).thenReturn(Collections.emptyList());
        when(securityManagementService.getCertificateAssociatedDevicesNames(cert2)).thenReturn(Collections.singletonList(deviceName));
        when(securityManagementService.getCertificateAssociatedDevicesNames(cert3)).thenReturn(Collections.emptyList());

        when(securityManagementService.streamDirectoryCertificateUsages())
                .thenReturn(FakeBuilder.initBuilderStub(Collections.emptyList(), QueryStream.class));
        when(accessor.getSecurityAccessorType()).thenReturn(accessorType);
        when(accessorType.getName()).thenReturn(accessorName);
        when(dirUsage.getDirectoryName()).thenReturn(dirUsageName);

        //Act
        Response response = target("/certificates/checkBulkRevoke").request().post(Entity.json(requestInfo));

        //Verify
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Boolean>get("isOnline")).isTrue();
        assertThat(model.<Integer>get("bulk.total")).isEqualTo(3);
        assertThat(model.<Integer>get("bulk.valid")).isEqualTo(1);
        assertThat(model.<Integer>get("bulk.invalid")).isEqualTo(2);
        assertThat(model.<JSONArray>get("bulk.withUsages")).hasSize(1);
        assertThat(model.<JSONObject>get("bulk.withUsages[0]").get("id")).isEqualTo(certId2.intValue());
        assertThat(model.<JSONArray>get("bulk.withWrongStatus")).hasSize(1);
        assertThat(model.<JSONObject>get("bulk.withWrongStatus[0]").get("id")).isEqualTo(certId3.intValue());
        assertThat(model.<JSONArray>get("bulk.certificatesIds")).hasSize(3).contains(certId1.intValue(), certId2.intValue(), certId3.intValue());
    }

    @Test
    public void testBulkRevokeCertificate() throws Exception {
        //Prepare
        Long certId1 = 223L;
        Long certId2 = 224L;
        Long certId3 = 225L;
        String certAlias1 = "al1";
        String certAlias2 = "al2";
        String certAlias3 = "al3";
        Long timeout = 10L;

        ArgumentCaptor<List<CertificateWrapper>> captor = ArgumentCaptor.forClass((Class)List.class);
        CertificateWrapper cert1 = mock(CertificateWrapper.class);
        CertificateWrapper cert2 = mock(CertificateWrapper.class);
        CertificateWrapper cert3 = mock(CertificateWrapper.class);

        when(cert1.getId()).thenReturn(certId1);
        when(cert1.getAlias()).thenReturn(certAlias1);
        when(cert1.getCertificateStatus()).thenReturn(Optional.of(CertificateStatus.AVAILABLE));
        when(cert2.getId()).thenReturn(certId2);
        when(cert2.getAlias()).thenReturn(certAlias2);
        when(cert2.getCertificateStatus()).thenReturn(Optional.of(CertificateStatus.AVAILABLE));
        when(cert3.getId()).thenReturn(certId3);
        when(cert3.getAlias()).thenReturn(certAlias3);
        when(cert3.getCertificateStatus()).thenReturn(Optional.of(CertificateStatus.AVAILABLE));

        CertificateRevocationInfo requestInfo = new CertificateRevocationInfo();
        requestInfo.timeout = timeout;
        requestInfo.bulk.certificatesIds = Arrays.asList(certId1, certId2, certId3);

        CertificateRevocationResultInfo resultInfo = new CertificateRevocationResultInfo();
        resultInfo.addRevoked(cert1);
        resultInfo.addWithError(cert2);

        when(revocationUtils.findAllCertificateWrappers(Arrays.asList(certId1, certId2, certId3))).thenReturn(Arrays.asList(cert1, cert2, cert3));
        when(revocationUtils.isCAConfigured()).thenReturn(true);
        when(securityManagementService.getAssociatedCertificateAccessors(cert1)).thenReturn(Collections.emptyList());
        when(securityManagementService.getAssociatedCertificateAccessors(cert2)).thenReturn(Collections.emptyList());
        when(securityManagementService.getAssociatedCertificateAccessors(cert3)).thenReturn(Collections.emptyList());
        when(securityManagementService.getCertificateAssociatedDevicesNames(cert1)).thenReturn(Collections.emptyList());
        when(securityManagementService.getCertificateAssociatedDevicesNames(cert2)).thenReturn(Collections.emptyList());
        when(securityManagementService.getCertificateAssociatedDevicesNames(cert3)).thenReturn(Collections.singletonList("device"));
        when(securityManagementService.streamDirectoryCertificateUsages())
                .thenReturn(FakeBuilder.initBuilderStub(Collections.emptyList(), QueryStream.class));
        when(revocationUtils.bulkRevokeCertificates(captor.capture(), eq(timeout))).thenReturn(resultInfo);

        //Act
        Response response = target("/certificates/bulkRevoke").request().post(Entity.json(requestInfo));

        //Verify
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(captor.getValue()).contains(cert1, cert2);

        //get known order to verify
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("totalCount")).isEqualTo(3);
        assertThat(model.<Integer>get("revokedCount")).isEqualTo(1);
        assertThat(model.<Integer>get("withErrorsCount")).isEqualTo(1);
        assertThat(model.<Integer>get("withUsagesCount")).isEqualTo(1);

        JSONArray revoked = model.get("revoked");
        JSONArray withErrors = model.get("withErrors");
        JSONArray withUsages = model.get("withUsages");

        assertThat(revoked).hasSize(1);
        JSONObject revokedInfo = (JSONObject) revoked.get(0);
        assertThat(revokedInfo.get("id")).isEqualTo(certId1.intValue());
        assertThat(revokedInfo.get("name")).isEqualTo(certAlias1);

        assertThat(withErrors).hasSize(1);
        JSONObject withErrorsInfo = (JSONObject) withErrors.get(0);
        assertThat(withErrorsInfo.get("id")).isEqualTo(certId2.intValue());
        assertThat(withErrorsInfo.get("name")).isEqualTo(certAlias2);

        assertThat(withUsages).hasSize(1);
        JSONObject withUsagesInfo = (JSONObject) withUsages.get(0);
        assertThat(withUsagesInfo.get("id")).isEqualTo(certId3.intValue());
        assertThat(withUsagesInfo.get("name")).isEqualTo(certAlias3);

        verify(revocationUtils, times(1)).bulkRevokeCertificates(any(), eq(timeout));
    }

    @Test
    public void testRequestCertificateFromCA() throws Exception {
        Long certId = 345L;
        ClientCertificateWrapper certificateWrapper = mock(ClientCertificateWrapper.class);
        when(securityManagementService.findCertificateWrapper(certId)).thenReturn(Optional.of(certificateWrapper));
        PKCS10CertificationRequest csr = mock(PKCS10CertificationRequest.class);
        X509Certificate x509Certificate = loadCertificate("myRootCA.cert");
        when(certificateWrapper.getCSR()).thenReturn(Optional.of(csr));
        when(certificateWrapper.getCertificateRequestData()).thenReturn(Optional.empty());
        when(caService.signCsr(csr,Optional.empty())).thenReturn(x509Certificate);
        Response response = target("/certificates/" + certId + "/requestCertificate").request().post(null);

        //Verify
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(securityManagementService, times(1)).findCertificateWrapper(certId);
        verifyNoMoreInteractions(securityManagementService);
        verify(certificateWrapper, times(1)).setCertificate(x509Certificate, Optional.empty());
    }

    private X509Certificate loadCertificate(String name) throws IOException, CertificateException {
        return (X509Certificate) certificateFactory.generateCertificate(CertificateWrapperResourceTest.class.getResourceAsStream(name));
    }

}
