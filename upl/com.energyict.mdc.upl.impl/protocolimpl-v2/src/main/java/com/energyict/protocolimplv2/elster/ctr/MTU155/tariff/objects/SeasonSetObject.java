package com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.objects;

import com.energyict.cbo.BusinessException;
import com.energyict.mdw.core.Season;
import com.energyict.mdw.core.SeasonSet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 4/04/11
 * Time: 12:48
 */
public class SeasonSetObject implements Serializable {

    private int id;
    private String name;
    private List<SeasonObject> seasons;

    public SeasonSetObject() {

    }

    public static SeasonSetObject fromSeasonSet(SeasonSet seasonSet) {
        SeasonSetObject ss = new SeasonSetObject();
        ss.setId(seasonSet.getId());
        ss.setName(seasonSet.getName());
        List<Season> eiserverSeasons = seasonSet.getSeasons();
        ss.setSeasons(new ArrayList<SeasonObject>());
        for (Season eis : eiserverSeasons) {
            ss.getSeasons().add(SeasonObject.fromSeason(eis));
        }
        return ss;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<SeasonObject> getSeasons() {
        return seasons;
    }

    public void setSeasons(List<SeasonObject> seasons) {
        this.seasons = seasons;
    }

    public SeasonObject getSeason(int period) throws BusinessException {
        for (SeasonObject season : seasons) {
            if (season.isPeriod(period)) {
                return season;
            }
        }
        throw new BusinessException("No season found for period [" + period + "]");
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("SeasonSetObject");
        sb.append("{id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", seasons=").append(seasons);
        sb.append('}');
        return sb.toString();
    }

}
