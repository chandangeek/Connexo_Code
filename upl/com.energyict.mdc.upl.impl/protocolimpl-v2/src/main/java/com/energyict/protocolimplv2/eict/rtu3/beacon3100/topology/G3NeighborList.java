package com.energyict.protocolimplv2.eict.rtu3.beacon3100.topology;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;

import java.util.HashMap;
import java.util.Optional;

public class G3NeighborList {

    private final HashMap<Long, G3Neighbor> neighborHashMap = new HashMap<>();


    public void load(Array neighborTable) {
        neighborTable.getAllDataTypes().forEach(struct -> {
            if (struct.isStructure()){
                G3Neighbor neighbor = G3Neighbor.fromStructure((Structure) struct);

                neighborHashMap.put(neighbor.getShortAddress(), neighbor);
            }
        });
    }

    public boolean available(){
        return neighborHashMap.size()>0;
    }


    public HashMap<Long, G3Neighbor> getNeighborHashMap() {
        return neighborHashMap;
    }

    public Optional<G3Neighbor> findByShortAddress(long shortAddress){
        if (neighborHashMap.containsKey(shortAddress)){
            return Optional.of(neighborHashMap.get(shortAddress));
        } else {
            return Optional.empty();
        }
    }

    public void update(Array neighborTable) {
        neighborTable.getAllDataTypes().forEach(struct -> {
            if (struct.isStructure()){
                G3Neighbor neighbor = G3Neighbor.fromStructure((Structure) struct);

                boolean overwrite;

                if (neighborHashMap.containsKey(neighbor.getShortAddress())){
                    overwrite = false;

                    if (neighborHashMap.get(neighbor.getShortAddress()).getModulation().isKnown()) {
                        // this looks to be a valid value
                        overwrite = true;
                    }
                } else {
                    overwrite = true;
                }

                if (overwrite) {
                    neighborHashMap.put(neighbor.getShortAddress(), neighbor);
                }
            }
        });
    }
}
