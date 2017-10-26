/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.PrivateKeyWrapper;
import com.elster.jupiter.pki.rest.impl.CertificateInfoFactory;
import com.elster.jupiter.pki.rest.impl.CsrInfo;

import com.jayway.jsonpath.JsonModel;
import org.assertj.core.api.AssertionsForClassTypes;
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
import java.util.Optional;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
        assertThat(model.<String>get("issuer")).contains("C=BE","ST=Vlaanderen","L=Kortrijk","O=Honeywell","OU=SmartEnergy","CN=MyRootCA");
        assertThat(model.<String>get("subject")).contains("C=BE","ST=Vlaanderen","L=Kortrijk","O=Honeywell","OU=SmartEnergy","CN=MyRootCA");
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
    @Ignore
    public void testDownloadCSRButThereIsNone() throws Exception {
        ClientCertificateWrapper certificateWrapper = mock(ClientCertificateWrapper.class);
        when(securityManagementService.findCertificateWrapper(12345)).thenReturn(Optional.of(certificateWrapper));
        when(certificateWrapper.getCertificate()).thenReturn(Optional.empty());
        when(certificateWrapper.getCSR()).thenReturn(Optional.empty());
        when(certificateWrapper.hasCSR()).thenReturn(false);
        when(certificateWrapper.getAlias()).thenReturn("downloadedRootCa");


        Response response = target("/certificates/12345/download/csr").request().get();
        InputStream entity = (InputStream) response.getEntity();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
    }

    private X509Certificate loadCertificate(String name) throws IOException, CertificateException {
        return (X509Certificate) certificateFactory.generateCertificate(CertificateWrapperResourceTest.class.getResourceAsStream(name));
    }

}
