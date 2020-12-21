package com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.common.obis.matchers.ObisChannel;
import com.energyict.protocolimplv2.dlms.common.obis.readers.MappingException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;

@RunWith(MockitoJUnitRunner.class)
public class DynamicMapperTest {

    @Mock
    private AttributeMapper<AbstractDataType> mapAttr1;

    @Mock
    private AttributeMapper<AbstractDataType> mapAttr2;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void noActualReaders() throws MappingException {
        expectedException.expect(MappingException.class);
        DynamicMapper dynamicMapper = new DynamicMapper(new ArrayList<>());
        dynamicMapper.get(ObisChannel.A, ObisCode.fromString("1.2.3.4.5.6"));
    }

    @Test
    public void channelNotMapped() throws MappingException {
        expectedException.expect(MappingException.class);
        DynamicMapper dynamicMapper = new DynamicMapper(Arrays.asList(mapAttr1));
        dynamicMapper.get(ObisChannel.F, ObisCode.fromString("1.2.3.4.5.6"));
    }

    @Test
    public void mapAttr1Mapped() throws MappingException {
        DynamicMapper dynamicMapper = new DynamicMapper(Arrays.asList(mapAttr1));
        AttributeMapper<? extends AbstractDataType> mapper = dynamicMapper.get(ObisChannel.A, ObisCode.fromString("1.2.3.4.5.6"));
        Assert.assertEquals(mapAttr1, mapper);
    }

    @Test
    public void mapAttr2Mapped() throws MappingException {
        DynamicMapper dynamicMapper = new DynamicMapper(Arrays.asList(mapAttr1, mapAttr2));
        Assert.assertEquals(mapAttr1, dynamicMapper.get(ObisChannel.A, ObisCode.fromString("1.2.3.4.5.6")));
        Assert.assertEquals(mapAttr2, dynamicMapper.get(ObisChannel.B, ObisCode.fromString("1.2.3.4.5.6")));
    }

}