/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.pki.rest.impl.TrustStoreInfo;

import com.jayway.jsonpath.JsonModel;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TrustStoreResourceTest extends PkiApplicationTest {

    public String TRUST_STORE_NAME = "Whatever";

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        Security.addProvider(new BouncyCastleProvider());
    }

    private TrustStore mockTrustStore(String name, long id) {
        TrustStore trustStore = mock(TrustStore.class);
        when(trustStore.getId()).thenReturn(id);
        when(trustStore.getName()).thenReturn(name);
        when(trustStore.getDescription()).thenReturn("Description of trust store " + name);
        return trustStore;
    }

    @Test
    public void getAllTrustStores() {
        List<TrustStore> trustStores = new ArrayList<>();
        trustStores.add(mockTrustStore("store 1", 1001));
        trustStores.add(mockTrustStore("store 2", 1002));
        when(pkiService.getAllTrustStores()).thenReturn(trustStores);

        String response = target("/truststores").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(2);
        assertThat(model.<List>get("$.trustStores")).isNotEmpty();
    }

    @Test
    public void getTrustStore() throws Exception {
        TrustStore store = mockTrustStore(TRUST_STORE_NAME, 1001);
        when(pkiService.findTrustStore(1001)).thenReturn(Optional.of(store));

        String response = target("/truststores/" + 1001).request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<TrustStoreInfo>get("id")).isEqualTo(1001);
        assertThat(model.<TrustStoreInfo>get("name")).isEqualTo(TRUST_STORE_NAME);
        assertThat(model.<TrustStoreInfo>get("description")).isEqualTo("Description of trust store " + TRUST_STORE_NAME);
    }

    @Test
    public void testImportTrustedCertificate() throws Exception {
        TrustStore store = mockTrustStore(TRUST_STORE_NAME, 1001);
        when(pkiService.findTrustStore(1001)).thenReturn(Optional.of(store));

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

        Response response = target("/truststores/1001/certificates").
                request(MediaType.TEXT_PLAIN).
                post(Entity.entity(multiPart, MediaType.MULTIPART_FORM_DATA_TYPE));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<X509Certificate> certificateArgumentCaptor = ArgumentCaptor.forClass(X509Certificate.class);
        verify(store, times(1)).addCertificate(stringArgumentCaptor.capture(), certificateArgumentCaptor.capture());
        assertThat(stringArgumentCaptor.getValue()).isEqualTo("myCert");
        assertThat(certificateArgumentCaptor.getValue().getIssuerDN().getName()).contains("CN=MyRootCA");
    }
}
