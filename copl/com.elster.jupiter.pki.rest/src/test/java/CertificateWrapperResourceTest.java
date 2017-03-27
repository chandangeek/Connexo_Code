/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.pki.PrivateKeyWrapper;
import com.elster.jupiter.pki.rest.impl.CsrInfo;

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
import java.io.File;
import java.net.URL;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Optional;

import org.junit.Before;
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
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    public void testImportCertificate() throws Exception {
        CertificateWrapper certificateWrapper = mock(CertificateWrapper.class);
        when(pkiService.newCertificateWrapper(anyString())).thenReturn(certificateWrapper);

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
        verify(pkiService, times(1)).newCertificateWrapper(stringArgumentCaptor.capture());
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
        when(pkiService.getKeyType(123L)).thenReturn(Optional.of(keyType));
        PkiService.ClientCertificateWrapperBuilder builder = mock(PkiService.ClientCertificateWrapperBuilder.class);
        when(builder.alias("brandNew")).thenReturn(builder);
        ClientCertificateWrapper certificateWrapper = mock(ClientCertificateWrapper.class);
        when(builder.add()).thenReturn(certificateWrapper);
        PrivateKeyWrapper privateKeyWrapper = mock(PrivateKeyWrapper.class);
        when(certificateWrapper.getPrivateKeyWrapper()).thenReturn(privateKeyWrapper);
        when(pkiService.newClientCertificateWrapper(keyType, "vault")).thenReturn(builder);
        when(certificateWrapper.getCertificate()).thenReturn(Optional.empty());
        when(certificateWrapper.getExpirationTime()).thenReturn(Optional.empty());
        when(certificateWrapper.getAllKeyUsages()).thenReturn(Optional.empty());
        PKCS10CertificationRequest csr = mock(PKCS10CertificationRequest.class);
        when(certificateWrapper.hasCSR()).thenReturn(true);
        when(certificateWrapper.getCSR()).thenReturn(Optional.of(csr));
        X500Name x500Name = mock(X500Name.class);
        when(x500Name.toString()).thenReturn("x500");
        when(csr.getSubject()).thenReturn(x500Name);
        AlgorithmIdentifier signatureAlgorithm = mock(AlgorithmIdentifier.class);
        when(csr.getSignatureAlgorithm()).thenReturn(signatureAlgorithm);
        ASN1ObjectIdentifier algorithm = new ASN1ObjectIdentifier("1.2.840.10045.4.3.2");
        when(signatureAlgorithm.getAlgorithm()).thenReturn(algorithm);

        Response response = target("/certificates/csr").request().post(Entity.json(csrInfo));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

        ArgumentCaptor<X500Name> x500NameArgumentCaptor = ArgumentCaptor.forClass(X500Name.class);
        verify(certificateWrapper, times(1)).getPrivateKeyWrapper();
        verify(privateKeyWrapper, times(1)).generateValue();
        verify(certificateWrapper, times(1)).generateCSR(x500NameArgumentCaptor.capture());
        assertThat(x500NameArgumentCaptor.getValue().getRDNs());
    }
}
