package com.energyict.mdc.device.configuration.rest.impl;


import com.elster.jupiter.cbo.Commodity;
import com.energyict.mdc.common.rest.MapBasedXmlAdapter;

public class CommodityAdapter extends MapBasedXmlAdapter<Commodity> {

    public CommodityAdapter() {
        register("", Commodity.NOTAPPLICABLE);
        register("Not applicable", Commodity.NOTAPPLICABLE);
        register("Electricity primary metered",Commodity.ELECTRICITY_PRIMARY_METERED);
        register("Electricity secondary metered",Commodity.ELECTRICITY_SECONDARY_METERED);
        register("Communication",Commodity.COMMUNICATION);
        register("Air",Commodity.AIR );
        register("Insulative gas",Commodity.INSULATIVEGAS );
        register("Insulative oil",Commodity.INSULATIVEOIL );
        register("Natural gas",Commodity.NATURALGAS );
        register("Propane",Commodity.PROPANE );
        register("Potable water",Commodity.POTABLEWATER );
        register("Steam",Commodity.STEAM );
        register("Waste water",Commodity.WASTEWATER );
        register("Heating fluid",Commodity.HEATINGFLUID );
        register("Cooling fluid",Commodity.COOLINGFLUID);
        register("Non Potable Water",Commodity.NONPOTABLEWATER);
        register("NOx",Commodity.NOX);
        register("SO2",Commodity.SO2);
        register("CH4",Commodity.CH4);
        register("CO2",Commodity.CO2);
        register("Carbon",Commodity.CARBON);
        register("HCH",Commodity.HCH);
        register("PFC",Commodity.PFC);
        register("SF6",Commodity.SF6);
        register("TV License",Commodity.TVLICENSE);
        register("Internet",Commodity.INTERNET);
        register("Trash",Commodity.REFUSE);
        register("Hydrogen",Commodity.H2);
        register("Acetylene",Commodity.C2H2);
        register("Ethylene",Commodity.C2H4);
        register("Ethane",Commodity.C2H6);
        register("Carbon monoxide",Commodity.CO);
        register("Oxygen",Commodity.O2);
        register("Dissolved combustible gas",Commodity.DISSOLVEDCOMBUSTIBLEGAS);
        register("Carbon dioxide equivalent",Commodity.CO2E);
        register("lead",Commodity.LEAD);
        register("Mercury",Commodity.MERCURY);
        register("Ozone",Commodity.OZONE);
        register("Particulate matter maximum size 10 \u00B5m",Commodity.PM10);
        register("Particulate matter maximum size 2.5 \u00B5m",Commodity.PM25);
        register("Sulfur Oxides",Commodity.SOX);
        register("Weather or meteorological conditions",Commodity.WEATHER);
        register("End device condition",Commodity.DEVICE);
    }
}
