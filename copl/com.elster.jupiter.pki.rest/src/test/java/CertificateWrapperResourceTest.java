/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.CertificateWrapperStatus;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.DirectoryCertificateUsage;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.PrivateKeyWrapper;
import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.rest.impl.CertificateInfoFactory;
import com.elster.jupiter.pki.rest.impl.CertificateRevocationInfo;
import com.elster.jupiter.pki.rest.impl.CertificateRevocationResultInfo;
import com.elster.jupiter.pki.rest.impl.CsrInfo;
import com.elster.jupiter.util.conditions.Condition;

import com.jayway.jsonpath.JsonModel;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
import java.util.stream.Collectors;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

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
        CertificateWrapper certificateWrapper = mock(CertificateWrapper.class);
        when(securityManagementService.newCertificateWrapper(anyString())).thenReturn(certificateWrapper);

        String fileName = "myRootCA.cert";
        Form form = new Form();
        form.param("alias", "myCert");
        URL resource = TrustStoreResourceTest.class.getClassLoader().getResource(fileName);
        String path = resource.getPath();
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
        verify(certificateWrapper, times(1)).setCertificate(certificateArgumentCaptor.capture());
        assertThat(stringArgumentCaptor.getValue()).isEqualTo("myCert");
        assertThat(certificateArgumentCaptor.getValue().getIssuerDN().getName()).contains("CN=MyRootCA");
    }

    @Test
    public void testCreateCertificateWithWrapper() throws Exception {
        CsrInfo csrInfo = new CsrInfo();
        csrInfo.keyTypeId = 123L;
        csrInfo.keyEncryptionMethod = "vault";
        csrInfo.alias = "brandNew";
        csrInfo.CN = "lucifer";
        csrInfo.L = "hell";

        KeyType keyType = mock(KeyType.class);
        when(securityManagementService.getKeyType(123L)).thenReturn(Optional.of(keyType));
        SecurityManagementService.ClientCertificateWrapperBuilder builder = mock(SecurityManagementService.ClientCertificateWrapperBuilder.class);
        when(builder.alias("brandNew")).thenReturn(builder);
        ClientCertificateWrapper certificateWrapper = mock(ClientCertificateWrapper.class);
        when(builder.add()).thenReturn(certificateWrapper);
        PrivateKeyWrapper privateKeyWrapper = mock(PrivateKeyWrapper.class);
        when(certificateWrapper.getPrivateKeyWrapper()).thenReturn(privateKeyWrapper);
        when(securityManagementService.newClientCertificateWrapper(keyType, "vault")).thenReturn(builder);
        when(certificateWrapper.getCertificate()).thenReturn(Optional.empty());
        when(certificateWrapper.getExpirationTime()).thenReturn(Optional.empty());
        when(certificateWrapper.getAllKeyUsages()).thenReturn(Optional.empty());
        PKCS10CertificationRequest csr = mock(PKCS10CertificationRequest.class);
        when(certificateWrapper.hasCSR()).thenReturn(true);
        when(certificateWrapper.getCSR()).thenReturn(Optional.of(csr));
        X500Name x500Name = mock(X500Name.class);
        when(x500Name.toString()).thenReturn("cn=x500");
        when(csr.getSubject()).thenReturn(x500Name);
        AlgorithmIdentifier signatureAlgorithm = mock(AlgorithmIdentifier.class);
        when(csr.getSignatureAlgorithm()).thenReturn(signatureAlgorithm);
        ASN1ObjectIdentifier algorithm = new ASN1ObjectIdentifier("1.2.840.10045.4.3.2");
        when(signatureAlgorithm.getAlgorithm()).thenReturn(algorithm);

        Response response = target("/certificates/csr").request().post(Entity.json(csrInfo));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

        ArgumentCaptor<X500Name> x500NameArgumentCaptor = ArgumentCaptor.forClass(X500Name.class);
        verify(privateKeyWrapper, times(1)).generateValue();
        verify(certificateWrapper, times(1)).generateCSR(x500NameArgumentCaptor.capture());
        assertThat(x500NameArgumentCaptor.getValue().getRDNs());
    }

    @Test
    public void testGetCertificateWrapperWithCertificate() throws Exception {
        Clock clock = Clock.fixed(Instant.ofEpochMilli(1488240000000L), ZoneId.systemDefault());

        ClientCertificateWrapper certificateWrapper = mock(ClientCertificateWrapper.class);
        when(securityManagementService.findCertificateWrapper(12345)).thenReturn(Optional.of(certificateWrapper));
        when(certificateWrapper.getAlias()).thenReturn("root");
        when(certificateWrapper.getVersion()).thenReturn(135L);
        when(certificateWrapper.getCertificate()).thenReturn(Optional.of(loadCertificate("myRootCA.cert")));
        when(certificateWrapper.getExpirationTime()).thenReturn(Optional.of(Instant.now(clock)));
        when(certificateWrapper.getAllKeyUsages()).thenReturn(Optional.of("A, B, C"));
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
        assertThat(model.<String>get("serialNumber")).isEqualTo("12550491392904217459");
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
        when(certificateWrapper.getExpirationTime()).thenReturn(Optional.of(Instant.now(clock)));
        when(certificateWrapper.getAllKeyUsages()).thenReturn(Optional.of("A, B, C"));
        PrivateKeyWrapper privateKeyWrapper = mock(PrivateKeyWrapper.class);
        when(certificateWrapper.getPrivateKeyWrapper()).thenReturn(privateKeyWrapper);
        when(privateKeyWrapper.getKeyEncryptionMethod()).thenReturn("DataVault");


        Response response = target("/certificates/12345").request().get();
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<String>get("serialNumber")).isEqualTo("6585389233728085118907455613768833682118990666695162997537978648");
    }

    @Test
    public void testGetCNWithHtmlCharacters() throws Exception {
        Clock clock = Clock.fixed(Instant.ofEpochMilli(1488240000000L), ZoneId.systemDefault());

        ClientCertificateWrapper certificateWrapper = mock(ClientCertificateWrapper.class);
        when(securityManagementService.findCertificateWrapper(12345)).thenReturn(Optional.of(certificateWrapper));
        when(certificateWrapper.getAlias()).thenReturn("root");
        when(certificateWrapper.getVersion()).thenReturn(135L);
        when(certificateWrapper.getCertificate()).thenReturn(Optional.of(loadCertificate("fishy.cert.pem")));
        when(certificateWrapper.getExpirationTime()).thenReturn(Optional.of(Instant.now(clock)));
        when(certificateWrapper.getAllKeyUsages()).thenReturn(Optional.of("A, B, C"));
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
    public void testRenderSubject() throws Exception {
        CertificateInfoFactory certificateInfoFactory = new CertificateInfoFactory(null);
        String name = certificateInfoFactory.x500FormattedName("CN=Matthieu Deroo, OU=Software solutions, L=Kortrijk, ST=West-Vlaanderen, C=Belgium, O=Honeywell");
        AssertionsForClassTypes.assertThat(name).isEqualTo("CN=Matthieu Deroo, OU=Software solutions, O=Honeywell, L=Kortrijk, ST=West-Vlaanderen, C=Belgium");
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
        Query mockQuery = mock(Query.class);
        Long certId = 111L;
        ClientCertificateWrapper cert = mock(ClientCertificateWrapper.class);

        when(securityManagementService.findCertificateWrapper(certId)).thenReturn(Optional.of(cert));
        when(securityManagementService.getAssociatedCertificateAccessors(cert)).thenReturn(Collections.emptyList());
        when(securityManagementService.getCertificateAssociatedDevicesNames(cert)).thenReturn(Collections.emptyList());
        when(securityManagementService.getDirectoryCertificateUsagesQuery()).thenReturn(mockQuery);
        when(mockQuery.select(any(Condition.class))).thenReturn(Collections.emptyList());

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

        Query mockQuery = mock(Query.class);
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
        when(securityManagementService.getDirectoryCertificateUsagesQuery()).thenReturn(mockQuery);
        when(mockQuery.select(any(Condition.class))).thenReturn(Arrays.asList(dirUsage1, dirUsage2));
        when(accessor.getKeyAccessorType()).thenReturn(accessorType);
        when(accessorType.getName()).thenReturn(accessorName);
        when(dirUsage1.getDirectoryName()).thenReturn(dirUsageName1);
        when(dirUsage2.getDirectoryName()).thenReturn(dirUsageName2);

        //Act
        Response response = target("/certificates/" + certId + "/markObsolete").request().post(null);

        //Verify
        assertThat(response.getStatus()).isEqualTo(Response.Status.ACCEPTED.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<JSONArray>get("userDirectories")).hasSize(2).contains(dirUsageName1, dirUsageName2);
        assertThat(model.<JSONArray>get("importers")).isEmpty();
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

        Query mockQuery = mock(Query.class);

        Long certId = 222L;
        CertificateWrapper cert = mock(CertificateWrapper.class);

        when(securityManagementService.findCertificateWrapper(certId)).thenReturn(Optional.of(cert));
        when(securityManagementService.getAssociatedCertificateAccessors(cert)).thenReturn(Collections.emptyList());
        when(securityManagementService.getCertificateAssociatedDevicesNames(cert)).thenReturn(Collections.emptyList());
        when(securityManagementService.getDirectoryCertificateUsagesQuery()).thenReturn(mockQuery);
        when(mockQuery.select(any(Condition.class))).thenReturn(Collections.emptyList());
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

        Query mockQuery = mock(Query.class);
        DirectoryCertificateUsage dirUsage = mock(DirectoryCertificateUsage.class);
        SecurityAccessor accessor = mock(SecurityAccessor.class);
        SecurityAccessorType accessorType = mock(SecurityAccessorType.class);

        Long certId = 222L;
        CertificateWrapper cert = mock(CertificateWrapper.class);

        when(securityManagementService.findCertificateWrapper(certId)).thenReturn(Optional.of(cert));
        when(securityManagementService.getAssociatedCertificateAccessors(cert)).thenReturn(Collections.singletonList(accessor));
        when(securityManagementService.getCertificateAssociatedDevicesNames(cert)).thenReturn(Collections.singletonList(deviceName));
        when(securityManagementService.getDirectoryCertificateUsagesQuery()).thenReturn(mockQuery);
        when(mockQuery.select(any(Condition.class))).thenReturn(Collections.singletonList(dirUsage));
        when(accessor.getKeyAccessorType()).thenReturn(accessorType);
        when(accessorType.getName()).thenReturn(accessorName);
        when(dirUsage.getDirectoryName()).thenReturn(dirUsageName);


        //Act
        Response response = target("/certificates/" + certId + "/checkRevoke").request().post(null);

        //Verify
        assertThat(response.getStatus()).isEqualTo(Response.Status.ACCEPTED.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<JSONArray>get("userDirectories")).hasSize(1).contains(dirUsageName);
        assertThat(model.<JSONArray>get("importers")).isEmpty();
        assertThat(model.<JSONArray>get("devices")).hasSize(1).contains(deviceName);
        assertThat(model.<JSONArray>get("securityAccessors")).hasSize(1).contains(accessorName);
    }

    @Test
    public void testRevokeCertificate() throws Exception {
        //Prepare
        Long timeout = 10L;
        Long certId = 222L;
        CertificateWrapper cert = mock(CertificateWrapper.class);
        Query mockQuery = mock(Query.class);

        when(securityManagementService.findCertificateWrapper(certId)).thenReturn(Optional.of(cert));
        when(securityManagementService.getAssociatedCertificateAccessors(cert)).thenReturn(Collections.emptyList());
        when(securityManagementService.getCertificateAssociatedDevicesNames(cert)).thenReturn(Collections.emptyList());
        when(securityManagementService.getDirectoryCertificateUsagesQuery()).thenReturn(mockQuery);
        when(mockQuery.select(any(Condition.class))).thenReturn(Collections.emptyList());


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

        Query mockQuery = mock(Query.class);
        CertificateWrapper cert1 = mock(CertificateWrapper.class);
        CertificateWrapper cert2 = mock(CertificateWrapper.class);

        CertificateRevocationInfo requestInfo = new CertificateRevocationInfo();
        requestInfo.timeout = timeout;
        requestInfo.bulk.certificatesIds = Arrays.asList(certId1, certId2);

        when(cert1.getId()).thenReturn(certId1);
        when(cert2.getId()).thenReturn(certId2);
        when(revocationUtils.findAllCertificateWrappers(Arrays.asList(certId1, certId2))).thenReturn(Arrays.asList(cert1, cert2));
        when(revocationUtils.isCAConfigured()).thenReturn(true);
        when(securityManagementService.getAssociatedCertificateAccessors(cert1)).thenReturn(Collections.emptyList());
        when(securityManagementService.getAssociatedCertificateAccessors(cert2)).thenReturn(Collections.emptyList());
        when(securityManagementService.getCertificateAssociatedDevicesNames(cert1)).thenReturn(Collections.emptyList());
        when(securityManagementService.getCertificateAssociatedDevicesNames(cert2)).thenReturn(Collections.emptyList());
        when(securityManagementService.getDirectoryCertificateUsagesQuery()).thenReturn(mockQuery);
        when(mockQuery.select(any(Condition.class))).thenReturn(Collections.emptyList());

        //Act
        Response response = target("/certificates/checkBulkRevoke").request().post(Entity.json(requestInfo));

        //Verify
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Boolean>get("isOnline")).isTrue();
        assertThat(model.<Integer>get("bulk.total")).isEqualTo(2);
        assertThat(model.<Integer>get("bulk.valid")).isEqualTo(2);
        assertThat(model.<Integer>get("bulk.invalid")).isEqualTo(0);
        assertThat(model.<JSONArray>get("bulk.certificatesIdsWithUsages")).isEmpty();
        assertThat(model.<JSONArray>get("bulk.certificatesIds")).hasSize(2).contains(certId1.intValue(), certId2.intValue());
    }

    @Test
    public void testCheckBulkRevokeCertificate_usages() throws Exception {
        //Prepare
        Long certId1 = 211L;
        Long certId2 = 212L;
        Long timeout = 10L;
        String accessorName = "accessor";
        String deviceName = "device";
        String dirUsageName = "directory";

        Query mockQuery = mock(Query.class);
        DirectoryCertificateUsage dirUsage = mock(DirectoryCertificateUsage.class);
        SecurityAccessor accessor = mock(SecurityAccessor.class);
        SecurityAccessorType accessorType = mock(SecurityAccessorType.class);
        CertificateWrapper cert1 = mock(CertificateWrapper.class);
        CertificateWrapper cert2 = mock(CertificateWrapper.class);

        CertificateRevocationInfo requestInfo = new CertificateRevocationInfo();
        requestInfo.timeout = timeout;
        requestInfo.bulk.certificatesIds = Arrays.asList(certId1, certId2);

        when(cert1.getId()).thenReturn(certId1);
        when(cert2.getId()).thenReturn(certId2);
        when(revocationUtils.findAllCertificateWrappers(Arrays.asList(certId1, certId2))).thenReturn(Arrays.asList(cert1, cert2));
        when(revocationUtils.isCAConfigured()).thenReturn(true);
        when(securityManagementService.getAssociatedCertificateAccessors(cert1)).thenReturn(Collections.emptyList());
        when(securityManagementService.getAssociatedCertificateAccessors(cert2)).thenReturn(Collections.singletonList(accessor));
        when(securityManagementService.getCertificateAssociatedDevicesNames(cert1)).thenReturn(Collections.emptyList());
        when(securityManagementService.getCertificateAssociatedDevicesNames(cert2)).thenReturn(Collections.singletonList(deviceName));
        when(securityManagementService.getDirectoryCertificateUsagesQuery()).thenReturn(mockQuery);
        //return empty for first call (first certificate) and something for second one
        when(mockQuery.select(any(Condition.class))).thenReturn(Collections.emptyList()).thenReturn(Collections.singletonList(dirUsage));
        when(accessor.getKeyAccessorType()).thenReturn(accessorType);
        when(accessorType.getName()).thenReturn(accessorName);
        when(dirUsage.getDirectoryName()).thenReturn(dirUsageName);

        //Act
        Response response = target("/certificates/checkBulkRevoke").request().post(Entity.json(requestInfo));

        //Verify
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Boolean>get("isOnline")).isTrue();
        assertThat(model.<Integer>get("bulk.total")).isEqualTo(2);
        assertThat(model.<Integer>get("bulk.valid")).isEqualTo(1);
        assertThat(model.<Integer>get("bulk.invalid")).isEqualTo(1);
        assertThat(model.<JSONArray>get("bulk.certificatesIdsWithUsages")).hasSize(1).contains(certId2.intValue());
        assertThat(model.<JSONArray>get("bulk.certificatesIds")).hasSize(2).contains(certId1.intValue(), certId2.intValue());
    }

    @Test
    public void testBulkRevokeCertificate() throws Exception {
        //Prepare
        Long certId1 = 223L;
        Long certId2 = 224L;
        Long timeout = 10L;
        ArgumentCaptor<List<CertificateWrapper>> captor = ArgumentCaptor.forClass((Class)List.class);
        CertificateWrapper cert1 = mock(CertificateWrapper.class);
        CertificateWrapper cert2 = mock(CertificateWrapper.class);
        Query mockQuery = mock(Query.class);

        CertificateRevocationInfo requestInfo = new CertificateRevocationInfo();
        requestInfo.timeout = timeout;
        requestInfo.bulk.certificatesIds = Arrays.asList(certId1, certId2);

        CertificateRevocationResultInfo resultInfo = new CertificateRevocationResultInfo();
        resultInfo.addResult(String.valueOf(certId1), true);
        resultInfo.addResult(String.valueOf(certId2), false, "CA complaining");

        when(revocationUtils.findAllCertificateWrappers(Arrays.asList(certId1, certId2))).thenReturn(Arrays.asList(cert1, cert2));
        when(revocationUtils.isCAConfigured()).thenReturn(true);
        when(securityManagementService.getAssociatedCertificateAccessors(cert1)).thenReturn(Collections.emptyList());
        when(securityManagementService.getAssociatedCertificateAccessors(cert2)).thenReturn(Collections.emptyList());
        when(securityManagementService.getCertificateAssociatedDevicesNames(cert1)).thenReturn(Collections.emptyList());
        when(securityManagementService.getCertificateAssociatedDevicesNames(cert2)).thenReturn(Collections.emptyList());
        when(securityManagementService.getDirectoryCertificateUsagesQuery()).thenReturn(mockQuery);
        when(mockQuery.select(any(Condition.class))).thenReturn(Collections.emptyList());
        when(revocationUtils.bulkRevokeCertificates(captor.capture(), eq(timeout))).thenReturn(resultInfo);

        //Act
        Response response = target("/certificates/bulkRevoke").request().post(Entity.json(requestInfo));

        //Verify
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(captor.getValue()).contains(cert1, cert2);

        //get known order to verify
        List<JSONObject> results = JsonModel.model((InputStream) response.getEntity()).<JSONArray>get("revocationResults").stream()
                .map(o -> (JSONObject) o)
                .sorted((o1, o2) -> {
                    String name1 = (String) o1.get("alias");
                    String name2 = (String) o2.get("alias");
                    return name1.compareTo(name2);
                })
                .collect(Collectors.toList());
        assertThat(results).hasSize(2);
        assertThat(results.get(0).get("alias")).isEqualTo(String.valueOf(certId1));
        assertThat(results.get(0).get("success")).isEqualTo(true);
        assertThat(results.get(0).get("error")).isEqualTo(null);
        assertThat(results.get(1).get("alias")).isEqualTo(String.valueOf(certId2));
        assertThat(results.get(1).get("success")).isEqualTo(false);
        assertThat(results.get(1).get("error")).isEqualTo("CA complaining");

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
        when(caService.signCsr(csr)).thenReturn(x509Certificate);
        Response response = target("/certificates/" + certId + "/requestCertificate").request().post(null);

        //Verify
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(securityManagementService, times(1)).findCertificateWrapper(certId);
        verifyNoMoreInteractions(securityManagementService);
        verify(certificateWrapper, times(1)).setCertificate(x509Certificate);
    }

    private X509Certificate loadCertificate(String name) throws IOException, CertificateException {
        return (X509Certificate) certificateFactory.generateCertificate(CertificateWrapperResourceTest.class.getResourceAsStream(name));
    }

}
