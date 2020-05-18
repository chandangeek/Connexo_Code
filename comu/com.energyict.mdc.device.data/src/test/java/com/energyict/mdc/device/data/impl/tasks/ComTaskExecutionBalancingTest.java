/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.util.conditions.Order;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.common.comserver.OutboundComPortPool;

import org.fest.util.Lists;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ComTaskExecutionBalancingTest {
    private ComTaskExecutionBalancing comTaskExecutionBalancing = new ComTaskExecutionBalancing();

    private OutboundComPort getMockedOutboundComPort(long id){
        OutboundComPort outboundComPort = mock(OutboundComPort.class);
        when(outboundComPort.getId()).thenReturn(id);
        return outboundComPort;
    }

    private OutboundComPortPool getMockedOutboundComPortPool(List<OutboundComPort> comPorts){
        OutboundComPortPool outboundComPortPool = mock(OutboundComPortPool.class);
        when(outboundComPortPool.getComPorts()).thenReturn(comPorts);
        return outboundComPortPool;
    }

    private ComServer getMockedComServer(long id){
        ComServer comServer = mock(ComServer.class);
        when(comServer.getId()).thenReturn(id);
        return comServer;
    }

    @Test
    public void getBalancingOrderForComPortAscTest(){
        OutboundComPort comPort1 = getMockedOutboundComPort(1);
        OutboundComPort comPort2 = getMockedOutboundComPort(2);
        OutboundComPortPool outboundComPortPool = getMockedOutboundComPortPool(Lists.newArrayList(comPort1,comPort2));
        Order order = comTaskExecutionBalancing.getBalancingOrder(Lists.newArrayList(outboundComPortPool), comPort1);
        assertThat(order).isNotNull();
        assertThat(order.ascending()).isTrue();
    }

    @Test
    public void getBalancingOrderForComPortDescTest(){
        OutboundComPort comPort1 = getMockedOutboundComPort(1);
        OutboundComPort comPort2 = getMockedOutboundComPort(2);
        OutboundComPortPool outboundComPortPool = getMockedOutboundComPortPool(Lists.newArrayList(comPort1,comPort2));
        Order order = comTaskExecutionBalancing.getBalancingOrder(Lists.newArrayList(outboundComPortPool), comPort2);
        assertThat(order).isNotNull();
        assertThat(order.ascending()).isFalse();
    }

    @Test
    public void getBalancingOrderForComPortAscWithMoreComPortPoolsTest(){
        OutboundComPort comPort1 = getMockedOutboundComPort(1);
        OutboundComPort comPort2 = getMockedOutboundComPort(2);
        OutboundComPortPool outboundComPortPool1 = getMockedOutboundComPortPool(Lists.newArrayList(comPort1,comPort2));
        OutboundComPortPool outboundComPortPool2 = getMockedOutboundComPortPool(Lists.newArrayList(comPort1));
        Order order = comTaskExecutionBalancing.getBalancingOrder(Lists.newArrayList(outboundComPortPool1, outboundComPortPool2), comPort1);
        assertThat(order).isNotNull();
        assertThat(order.ascending()).isTrue();
    }

    @Test
    public void getBalancingOrderForComPortDescWithMoreComPortPoolsTest(){
        OutboundComPort comPort1 = getMockedOutboundComPort(1);
        OutboundComPort comPort2 = getMockedOutboundComPort(2);
        OutboundComPortPool outboundComPortPool1 = getMockedOutboundComPortPool(Lists.newArrayList(comPort1,comPort2));
        OutboundComPortPool outboundComPortPool2 = getMockedOutboundComPortPool(Lists.newArrayList(comPort1));
        Order order = comTaskExecutionBalancing.getBalancingOrder(Lists.newArrayList(outboundComPortPool1, outboundComPortPool2), comPort2);
        assertThat(order).isNotNull();
        assertThat(order.ascending()).isFalse();
    }

    @Test
    public void getBalancingOrderForComPortAscWithMoreComPortPoolsAndDifferentOrderTest(){
        OutboundComPort comPort1 = getMockedOutboundComPort(1);
        OutboundComPort comPort2 = getMockedOutboundComPort(2);
        OutboundComPortPool outboundComPortPool1 = getMockedOutboundComPortPool(Lists.newArrayList(comPort1,comPort2));
        OutboundComPortPool outboundComPortPool2 = getMockedOutboundComPortPool(Lists.newArrayList(comPort2,comPort1));
        Order order = comTaskExecutionBalancing.getBalancingOrder(Lists.newArrayList(outboundComPortPool1, outboundComPortPool2), comPort1);
        assertThat(order).isNotNull();
        assertThat(order.ascending()).isTrue();
    }

    @Test
    public void getBalancingOrderForComPortWithMoreComPortPoolAndDifferentOrderTest(){
        OutboundComPort comPort1 = getMockedOutboundComPort(1);
        OutboundComPort comPort2 = getMockedOutboundComPort(2);
        OutboundComPort comPort3 = getMockedOutboundComPort(3);
        OutboundComPortPool outboundComPortPool1 = getMockedOutboundComPortPool(Lists.newArrayList(comPort1,comPort2));
        OutboundComPortPool outboundComPortPool2 = getMockedOutboundComPortPool(Lists.newArrayList(comPort3));
        Order order = comTaskExecutionBalancing.getBalancingOrder(Lists.newArrayList(outboundComPortPool1, outboundComPortPool2), comPort1);
        assertThat(order).isNotNull();
        assertThat(order.ascending()).isTrue();
        order = comTaskExecutionBalancing.getBalancingOrder(Lists.newArrayList(outboundComPortPool1, outboundComPortPool2), comPort2);
        assertThat(order).isNotNull();
        assertThat(order.ascending()).isFalse();
        order = comTaskExecutionBalancing.getBalancingOrder(Lists.newArrayList(outboundComPortPool1, outboundComPortPool2), comPort3);
        assertThat(order).isNotNull();
        assertThat(order.ascending()).isTrue();
    }


    @Test
    public void getBalancingOrderForComServerAscTest(){
        ComServer comServer1 = getMockedComServer(1);
        ComServer comServer2 = getMockedComServer(2);
        Order order = comTaskExecutionBalancing.getBalancingOrder(Lists.newArrayList(comServer1, comServer2), comServer1);
        assertThat(order).isNotNull();
        assertThat(order.ascending()).isTrue();
    }

    @Test
    public void getBalancingOrderForComServerDescTest(){
        ComServer comServer1 = getMockedComServer(1);
        ComServer comServer2 = getMockedComServer(2);
        Order order = comTaskExecutionBalancing.getBalancingOrder(Lists.newArrayList(comServer1, comServer2), comServer2);
        assertThat(order).isNotNull();
        assertThat(order.ascending()).isFalse();
    }

}
