package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.rest.BigDecimalAsStringAdapter;
import org.junit.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by bvn on 2/16/15.
 */
public class BigDecimalAdapterTest {

    @Test
    public void testMarshal() throws Exception {
        BigDecimalAsStringAdapter adapter = new BigDecimalAsStringAdapter();
        assertThat(adapter.marshal(BigDecimal.valueOf(0.34343435))).isEqualTo("0.34343435");

    }

}
