import com.elster.jupiter.pki.CryptographicType;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */
public class KeyEncryptionMethodResourceTest extends PkiApplicationTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(pkiService.getKeyEncryptionMethods(CryptographicType.AsymmetricKey)).thenReturn(Arrays.asList("AS1", "AS2"));
        when(pkiService.getKeyEncryptionMethods(CryptographicType.SymmetricKey)).thenReturn(Arrays.asList("SYM1", "SYM2", "SYM3"));
        when(pkiService.getKeyEncryptionMethods(CryptographicType.Passphrase)).thenReturn(Arrays.asList("P1"));
    }

    @Test
    public void testGetAsymmetricKeyTypes() throws Exception {
        Response response = target("/keyencryptionmethods/asymmetric").request().get();
        JsonModel jsonModel = JsonModel.model((InputStream)response.getEntity());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(2);
        assertThat(jsonModel.<List<String>>get("$.keyEncryptionMethods[*].name")).containsOnly("AS1", "AS2");
    }

    @Test
    public void testGetSymmetricKeyTypes() throws Exception {
        Response response = target("/keyencryptionmethods/symmetric").request().get();
        JsonModel jsonModel = JsonModel.model((InputStream)response.getEntity());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(3);
        assertThat(jsonModel.<List<String>>get("$.keyEncryptionMethods[*].name")).containsOnly("SYM1", "SYM2", "SYM3");
    }
}
