package com.elster.jupiter.validation.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.DataModel;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.Arrays;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ChannelValidationImplTest extends EqualsContractTest {

    public static final long ID = 15L;

    private ChannelValidationImpl a;

    @Mock
    private Channel channel, channel1;
    @Mock
    private MeterActivationValidation meterActivationValidation, meterActivationValidation1;
    @Mock
    private DataModel dataModel;
    @Mock
    private MeteringService meteringService;
    @Mock
    private MeterActivation meterActivation, meterActivation1;

    @Override
    protected Object getInstanceA() {
        if (a == null) {
            setUp();
            a = ChannelValidationImpl.from(dataModel, meterActivationValidation, channel);
        }
        return a;
    }

    private void setUp() {
        when(dataModel.getInstance(ChannelValidationImpl.class)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return new ChannelValidationImpl();
            }
        });
        when(channel.getMeterActivation()).thenReturn(meterActivation);
        when(channel1.getMeterActivation()).thenReturn(meterActivation);
        when(meterActivationValidation.getMeterActivation()).thenReturn(meterActivation);
        when(meterActivationValidation1.getMeterActivation()).thenReturn(meterActivation);
    }

    @Override
    protected Object getInstanceEqualToA() {
        ChannelValidationImpl channelValidation = ChannelValidationImpl.from(dataModel, meterActivationValidation, channel);
        return channelValidation;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return Arrays.asList(
                ChannelValidationImpl.from(dataModel, meterActivationValidation1, channel),
                ChannelValidationImpl.from(dataModel, meterActivationValidation, channel1),
                ChannelValidationImpl.from(dataModel, meterActivationValidation1, channel1)
                );
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }
}