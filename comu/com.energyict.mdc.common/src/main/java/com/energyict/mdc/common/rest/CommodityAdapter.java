/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.rest;


import com.elster.jupiter.cbo.Commodity;

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
        register("Nitrous oxides (NO\u2093)",Commodity.NOX);
        register("Sulfur dioxide (SO\u2082)",Commodity.SO2);
        register("Methane (CH\u2084)",Commodity.CH4);
        register("Carbon dioxide (CO\u2082)",Commodity.CO2);
        register("Carbon",Commodity.CARBON);
        register("Hexachlorocyclohexane (HCH)",Commodity.HCH);
        register("Perfluorocarbons (PFC)",Commodity.PFC);
        register("Sulfurhexafluoride (SF\u2086)",Commodity.SF6);
        register("TV License",Commodity.TVLICENSE);
        register("Internet",Commodity.INTERNET);
        register("Trash",Commodity.REFUSE);
        register("Hydrogen (H\u2082)",Commodity.H2);
        register("Acetylene (C\u2082H\u2082)",Commodity.C2H2);
        register("Ethylene (C\u2082H\u2084)",Commodity.C2H4);
        register("Ethane (C\u2082H\u2086)",Commodity.C2H6);
        register("Carbon monoxide (CO)",Commodity.CO);
        register("Oxygen (O\u2082)",Commodity.O2);
        register("Dissolved combustible gas",Commodity.DISSOLVEDCOMBUSTIBLEGAS);
        register("Carbon dioxide equivalent (CO\u2082 equivalent)",Commodity.CO2E);
        register("lead",Commodity.LEAD);
        register("Mercury",Commodity.MERCURY);
        register("Ozone",Commodity.OZONE);
        register("Particulate matter < 10 \u00B5m",Commodity.PM10);
        register("Particulate matter < 2.5 \u00B5m",Commodity.PM25);
        register("Sulfur Oxides (SO\u2093)",Commodity.SOX);
        register("Weather or meteorological conditions",Commodity.WEATHER);
        register("End device condition",Commodity.DEVICE);
    }
}
