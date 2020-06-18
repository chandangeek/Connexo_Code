/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.conditions.Order;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.common.comserver.OutboundComPortPool;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFields;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.ToIntFunction;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class ComTaskExecutionBalancing {
    private static final String FIELD = ComTaskExecutionFields.CONNECTIONTASK.fieldName();
    private static final Collector<Integer, ?, Map<Boolean, List<Integer>>> indexesCollector = Collectors.groupingBy(index -> index % 2 == 0);
    private static final Comparator<OutboundComPort> portComparator = Comparator.comparing(OutboundComPort::getId);
    private static final ToIntFunction<Map.Entry<Boolean, List<Integer>>> indexesCountMapper = e -> (e.getKey() ? 1 : -1) * e.getValue().size();

    private <T extends HasId> int getElementIndex(List<T> elements, T element) {
        if (elements != null && element != null) {
            for (int i = 0; i < elements.size(); i++) {
                if (elements.get(i).getId() == element.getId()) {
                    return i;
                }
            }
        }
        return -1;
    }

    private List<OutboundComPort> sortComPorts(List<OutboundComPort> comPortsToSort) {
        List<OutboundComPort> sortedComPorts = Collections.emptyList();
        if (comPortsToSort != null) {
            sortedComPorts = comPortsToSort.stream().sorted(portComparator).collect(Collectors.toList());
        }
        return sortedComPorts;
    }

    private int reduceIndexes(Map<Boolean, List<Integer>> groupedIndexes) {
        if (groupedIndexes == null || groupedIndexes.isEmpty()) {
            return 0;
        }
        return groupedIndexes.entrySet().stream().mapToInt(indexesCountMapper).sum();
    }

    private int getOrderingIndicator(List<Integer> indexes) {
        return Optional.ofNullable(indexes).map(Collection::stream)
                .map(s -> s.collect(indexesCollector))
                .map(this::reduceIndexes).orElse(0);
    }

    private List<Integer> getComPortIndexes(List<OutboundComPortPool> comPortPools, OutboundComPort comPort) {
        List<Integer> indexes = Collections.emptyList();
        if (comPortPools != null && comPort != null) {
            indexes = comPortPools.stream().map(OutboundComPortPool::getComPorts)
                    .map(this::sortComPorts)
                    .map(list -> getElementIndex(list, comPort))
                    .filter(index -> index >= 0)
                    .collect(Collectors.toList());
        }
        return indexes;
    }

    public Order getBalancingOrder(List<OutboundComPortPool> comPortPools, OutboundComPort comPort) {
        boolean isAsc = isAscending(comPortPools, comPort);
        return isAsc ? Order.ascending(FIELD) : Order.descending(FIELD);
    }

    public boolean isAscending(List<OutboundComPortPool> comPortPools, OutboundComPort comPort) {
        int orderingIndicator = getOrderingIndicator(getComPortIndexes(comPortPools, comPort));
        return (orderingIndicator >= 0) && ((orderingIndicator != 0) || Math.random() < 0.5);
    }

    public Order getBalancingOrder(List<ComServer> comServers, ComServer comServer) {
        boolean isAsc = getElementIndex(comServers, comServer) % 2 == 0;
        return isAsc ? Order.ascending(FIELD) : Order.descending(FIELD);
    }
}
