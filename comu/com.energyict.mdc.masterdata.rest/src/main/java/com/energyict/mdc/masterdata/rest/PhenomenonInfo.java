package com.energyict.mdc.masterdata.rest;

import com.energyict.mdc.common.interval.Phenomenon;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class PhenomenonInfo {

    public int id;
    public String name;

    public static List<PhenomenonInfo> from(Collection<Phenomenon> phenomenons){
        List<PhenomenonInfo> infos = new ArrayList<>(phenomenons.size());
        for (Phenomenon phenomenon : phenomenons) {
            infos.add(PhenomenonInfo.from(phenomenon));
        }
        return infos;
    }

    public static PhenomenonInfo from(Phenomenon phenomenon){
        PhenomenonInfo info = new PhenomenonInfo();
        info.id = (int) phenomenon.getId();
        info.name = phenomenon.getName();
        return info;
    }
}
