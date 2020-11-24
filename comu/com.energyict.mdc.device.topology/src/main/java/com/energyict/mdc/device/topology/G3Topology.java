package com.energyict.mdc.device.topology;


import com.energyict.mdc.common.device.data.Device;

import java.util.*;

public interface G3Topology {

    void setGateway(Device gateway);

    void addG3NeighborLink(G3Neighbor g3Neighbor);

    List<G3Neighbor> getReferences();
}
