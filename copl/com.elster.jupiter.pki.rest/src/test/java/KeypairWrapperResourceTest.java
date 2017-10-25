import com.elster.jupiter.devtools.rest.MockUtils;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.KeypairWrapper;

import com.jayway.jsonpath.JsonModel;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 9/26/17.
 */
public class KeypairWrapperResourceTest extends PkiApplicationTest {
    final MockUtils mockUtils = new MockUtils();

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    public void testGetAllKeyPairs() throws Exception {
        KeypairWrapper keypair1 = mockKeypair("pair 1", 123L);

        Finder<KeypairWrapper> keypairFinder = mockUtils.mockFinder(Arrays.asList(keypair1));
        when(securityManagementService.findAllKeypairs()).thenReturn(keypairFinder);

        Response response = target("/keypairs").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<String>get("$.keypairs[0].alias")).isEqualTo("pair 1");
        assertThat(model.<Integer>get("$.keypairs[0].id")).isEqualTo(123);
        assertThat(model.<Boolean>get("$.keypairs[0].hasPublicKey")).isEqualTo(true);
        assertThat(model.<Boolean>get("$.keypairs[0].hasPrivateKey")).isEqualTo(true);
        assertThat(model.<String>get("$.keypairs[0].keyEncryptionMethod")).isEqualTo("DataVault");
        assertThat(model.<String>get("$.keypairs[0].keyType.name")).isEqualTo("key type");
        assertThat(model.<Integer>get("$.keypairs[0].keyType.id")).isEqualTo(7);

    }

    @Test
    public void testDeleteKeypair() throws Exception {
        KeypairWrapper keypair1 = mockKeypair("pair 1", 123L);
        when(securityManagementService.findKeypairWrapper(123L)).thenReturn(Optional.of(keypair1));

        Response delete = target("/keypairs/123").request().delete();
        assertThat(delete.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(keypair1, times(1)).delete();
    }

    @Test
    public void testDownloadPublicKey() throws Exception {
        KeypairWrapper keypair1 = mockKeypair("pair 1", 123L);
        KeyPairGenerator rsa = KeyPairGenerator.getInstance("RSA");
        rsa.initialize(1024);
        KeyPair keyPair = rsa.generateKeyPair();
        when(keypair1.getPublicKey()).thenReturn(Optional.of(keyPair.getPublic()));
        when(securityManagementService.findKeypairWrapper(123L)).thenReturn(Optional.of(keypair1));

        Response response = target("/keypairs/123/download/publickey").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        InputStream entity = (InputStream) response.getEntity();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(entity).hasSameContentAs(new ByteArrayInputStream(keyPair.getPublic().getEncoded()));
    }



    private KeypairWrapper mockKeypair(String alias, long id) {
        KeypairWrapper keypair1 = mock(KeypairWrapper.class);
        when(keypair1.getId()).thenReturn(id);
        when(keypair1.getKeyEncryptionMethod()).thenReturn(Optional.of("DataVault"));
        when(keypair1.getAlias()).thenReturn(alias);
        when(keypair1.hasPrivateKey()).thenReturn(true);
        PublicKey publicKey = mock(PublicKey.class);
        when(keypair1.getPublicKey()).thenReturn(Optional.ofNullable(publicKey));
        KeyType keyType = mock(KeyType.class);
        when(keyType.getName()).thenReturn("key type");
        when(keyType.getId()).thenReturn(7L);
        when(keypair1.getKeyType()).thenReturn(keyType);
        return keypair1;
    }
}
