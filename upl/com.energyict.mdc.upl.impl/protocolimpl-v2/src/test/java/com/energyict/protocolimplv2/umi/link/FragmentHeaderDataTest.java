package com.energyict.protocolimplv2.umi.link;

import com.energyict.protocolimplv2.umi.util.Limits;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FragmentHeaderDataTest {
    @Test
    public void createHeaderPayloadAndCheckFields() {
        int fullLength = 2056;
        FragmentHeaderData fragmentHeaderData = new FragmentHeaderData(FragmentType.FIRST, fullLength);

        assertEquals(FragmentType.FIRST, fragmentHeaderData.getFragmentType());
        assertEquals(2056, fragmentHeaderData.getData());
    }

    @Test
    public void createHeaderPayloadWithMaxLength() {
        int fullLength = Limits.MAX_UNSIGNED_SHORT + 1;
        FragmentHeaderData fragmentHeaderData = new FragmentHeaderData(FragmentType.FIRST, fullLength);

        assertEquals(FragmentType.FIRST, fragmentHeaderData.getFragmentType());
        assertEquals(0x0000, fragmentHeaderData.getData());
    }

    @Test
    public void createHeaderPayloadWithoutData() {
        int fullLength = 2056;
        FragmentHeaderData fragmentHeaderData = new FragmentHeaderData(FragmentType.ACK, fullLength);

        assertEquals(FragmentType.ACK, fragmentHeaderData.getFragmentType());
        assertEquals(0x0000, fragmentHeaderData.getData());
    }

    @Test
    public void createHeaderPayloadFromRaw() {
        FragmentationError fragmentationError = FragmentationError.LENGTH_ERROR;
        FragmentHeaderData fragmentHeaderData = new FragmentHeaderData(FragmentType.ERROR, fragmentationError.getId());
        FragmentHeaderData fragmentHeaderDataFromRaw = new FragmentHeaderData(fragmentHeaderData.getRaw());

        assertEquals(FragmentType.ERROR, fragmentHeaderDataFromRaw.getFragmentType());
        assertEquals(fragmentationError, FragmentationError.fromId(fragmentHeaderDataFromRaw.getData()));
    }
}
