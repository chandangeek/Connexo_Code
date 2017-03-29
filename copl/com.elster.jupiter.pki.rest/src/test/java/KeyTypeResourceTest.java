/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.pki.CryptographicType;
import com.elster.jupiter.pki.KeyType;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 3/29/17.
 */
public class KeyTypeResourceTest extends PkiApplicationTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        List<KeyType> keyTypes = Arrays.asList(
                mockKeyType(1, "AES 128", CryptographicType.SymmetricKey),
                mockKeyType(2, "AES 256", CryptographicType.SymmetricKey),
                mockKeyType(3, "NIST P-256", CryptographicType.AsymmetricKey),
                mockKeyType(5, "TLS", CryptographicType.ClientCertificate),
                mockKeyType(6, "BEACON", CryptographicType.Certificate),
                mockKeyType(7, "SUBCA", CryptographicType.TrustedCertificate),
                mockKeyType(4, "NIST P-358", CryptographicType.AsymmetricKey));
        when(pkiService.getKeyTypes()).thenReturn(keyTypes);
    }

    @Test
    public void testGetAllKeyTypes() throws Exception {
        Response response = target("/keytypes").request().get();
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(7);
        assertThat(jsonModel.<List>get("$.keyTypes")).hasSize(7);
    }

    @Test
    public void testGetCsrKeyTypes() throws Exception {
        Response response = target("/keytypes/forCsrCreation").request().get();
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List>get("$.keyTypes")).hasSize(1);
        assertThat(jsonModel.<String>get("$.keyTypes[0].name")).isEqualTo("TLS");
    }

    @Test
    public void testGetAllAsymmetricKeyTypes() throws Exception {
        Response response = target("/keytypes")
                .queryParam("filter",ExtjsFilter.filter("CryptographicType", Collections.singletonList("AsymmetricKey")))
                .request()
                .get();
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(2);
        assertThat(jsonModel.<List>get("$.keyTypes")).hasSize(2);
        assertThat(jsonModel.<List<String>>get("$.keyTypes[*].name")).containsOnly("NIST P-256", "NIST P-358");
    }

    @Test
    public void testGetAllSymmetricKeyTypes() throws Exception {
        Response response = target("/keytypes")
                .queryParam("filter",ExtjsFilter.filter("CryptographicType", Collections.singletonList("SymmetricKey")))
                .request()
                .get();
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(2);
        assertThat(jsonModel.<List>get("$.keyTypes")).hasSize(2);
        assertThat(jsonModel.<List<String>>get("$.keyTypes[*].name")).containsOnly("AES 128", "AES 256");
    }

    @Test
    public void testGetAllCertificateKeyTypes() throws Exception {
        Response response = target("/keytypes")
                .queryParam("filter",ExtjsFilter.filter("CryptographicType", Arrays.asList("Certificate", "ClientCertificate", "TrustedCertificate")))
                .request()
                .get();
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(3);
        assertThat(jsonModel.<List>get("$.keyTypes")).hasSize(3);
        assertThat(jsonModel.<List<String>>get("$.keyTypes[*].name")).containsOnly("TLS", "BEACON", "SUBCA");
    }

    @Test
    public void testKeyTypesInvalidCryptoType() throws Exception {
        Response response = target("/keytypes")
                .queryParam("filter",ExtjsFilter.filter("CryptographicType", Arrays.asList("Certificate", "invalid", "TrustedCertificate")))
                .request()
                .get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.error")).isEqualTo("NoSuchCryptographicType");
    }

    private KeyType mockKeyType(long id, String name, CryptographicType cryptoType) {
        KeyType mock = mock(KeyType.class);
        when(mock.getName()).thenReturn(name);
        when(mock.getId()).thenReturn(id);
        when(mock.getCryptographicType()).thenReturn(cryptoType);
        return mock;
    }
}
