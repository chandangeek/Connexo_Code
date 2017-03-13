/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.pki.rest.impl.TrustStoreInfo;

import com.jayway.jsonpath.JsonModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TrustStoreResourceTest extends PkiApplicationTest {

    public String TRUST_STORE_NAME = "Whatever";

    private TrustStore mockTrustStore(String name) {
        TrustStore trustStore = mock(TrustStore.class);
        when(trustStore.getName()).thenReturn(name);
        when(trustStore.getDescription()).thenReturn("Description of trust store " + name);
        return trustStore;
    }

    @Test
    public void getAllTrustStores() {
        List<TrustStore> trustStores = new ArrayList<>();
        trustStores.add(mockTrustStore("store 1"));
        trustStores.add(mockTrustStore("store 2"));
        when(pkiService.getAllTrustStores()).thenReturn(trustStores);

        String response = target("/truststores").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(2);
        assertThat(model.<List>get("$.trustStores")).isNotEmpty();
    }

    @Test
    public void getTrustStore() throws Exception {
        TrustStore store = mockTrustStore(TRUST_STORE_NAME);
        when(pkiService.findTrustStore(TRUST_STORE_NAME)).thenReturn(Optional.of(store));

        String response = target("/truststores/" + TRUST_STORE_NAME).request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<TrustStoreInfo>get("name")).isEqualTo(TRUST_STORE_NAME);
        assertThat(model.<TrustStoreInfo>get("description")).isEqualTo("Description of trust store " + TRUST_STORE_NAME);
    }

}
