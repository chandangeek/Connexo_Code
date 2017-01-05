package com.energyict.protocolimplv2.common.objectserialization.codetable.objects;

import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;

import com.energyict.cbo.BusinessException;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

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

    public static SeasonSetObject fromSeasonSet(TariffCalendarExtractor.CalendarSeasonSet uplSeasonSet) {
        SeasonSetObject seasonSet = new SeasonSetObject();
        seasonSet.setId(Integer.parseInt(uplSeasonSet.id()));
        seasonSet.setName(uplSeasonSet.name());
        seasonSet.setSeasons(uplSeasonSet.seasons().stream().map(SeasonObject::fromSeason).collect(Collectors.toList()));
        return seasonSet;
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
