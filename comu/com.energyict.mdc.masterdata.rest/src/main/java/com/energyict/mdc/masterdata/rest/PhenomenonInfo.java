package com.energyict.mdc.masterdata.rest;

import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.Phenomenon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.energyict.mdc.common.rest.UnitAdapter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class PhenomenonInfo {

    public long id;
    public String localizedValue;
    @XmlJavaTypeAdapter(UnitAdapter.class)
    public Unit unit;

    public static List<PhenomenonInfo> from(Collection<Phenomenon> phenomenons){
        List<PhenomenonInfo> infos = new ArrayList<>(phenomenons.size());
        for (Phenomenon phenomenon : phenomenons) {
            infos.add(PhenomenonInfo.from(phenomenon));
        }
        return infos;
    }

    public static PhenomenonInfo from(Phenomenon phenomenon){
        PhenomenonInfo info = new PhenomenonInfo();
        info.id = phenomenon.getId();
        info.localizedValue = phenomenon.getName();
        info.unit = phenomenon.getUnit();
        return info;
    }
}
