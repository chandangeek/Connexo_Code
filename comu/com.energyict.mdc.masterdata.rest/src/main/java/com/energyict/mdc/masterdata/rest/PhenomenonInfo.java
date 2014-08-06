package com.energyict.mdc.masterdata.rest;

import com.energyict.mdc.common.interval.Phenomenon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class PhenomenonInfo {

    public long id;
    public String localizedValue;

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
        return info;
    }
}
