/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.DirectoryCertificateUsage;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.PrivateKeyWrapper;
import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.rest.impl.CertificateInfoFactory;
import com.elster.jupiter.pki.rest.impl.CsrInfo;
import com.elster.jupiter.util.conditions.Condition;

import com.jayway.jsonpath.JsonModel;
import net.minidev.json.JSONArray;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static com.elster.jupiter.pki.rest.impl.MessageSeeds.NO_CSR_PRESENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
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
        verify(cert, times(1)).setObsolete(true);
    }

    @Test
    public void testMarkCertificateObsolete_thereAreUsages() throws Exception {
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
        verify(cert, never()).setObsolete(true);
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
        verify(certificateWrapper, times(1)).setObsolete(true);
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
        verify(certificateWrapper, times(1)).setObsolete(false);
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
