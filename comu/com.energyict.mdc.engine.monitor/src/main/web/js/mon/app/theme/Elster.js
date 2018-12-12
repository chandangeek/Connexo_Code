/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

// Elster Corporate Identity Colors
Ext.define('CSMonitor.theme.Elster',{
    alternateClassName: ['Elster'],
    statics:{'BLUE': '#002244',
             'OCEAN_BLUE': '#0073CF',
             'SKY_BLUE': '#00B9E4',
             'STEEL_GRAY': '#949D9E',
             'DARK_GREY': '#51626F',
             'BLACK': '#1C1E1C',
             'GRASS_GREEN': '#009B3A',
             'LIME_GREEN': '#92D400',
             'SUNSHINE_YELLOW': '#FDC82F',
             'FIRE_ORANGE': '#FF5800'
    }
});

// Elser Custom Chart Theme
Ext.define('Ext.chart.theme.Elster',{
    extend : 'Ext.chart.theme.Base',
    requires: ['Ext.chart.theme.Theme'],
    constructor: function(config) {
        Ext.chart.theme.Base.prototype.constructor.call(this, Ext.apply({
            colors: [Elster.OCEAN_BLUE,
                     Elster.LIME_GREEN,
                     Elster.BLUE,
                     Elster.SUNSHINE_YELLOW,
                     Elster.SKY_BLUE,
                     Elster.STEEL_GRAY,
                     Elster.DARK_GREY,
                     Elster.BLACK,
                     Elster.GRASS_GREEN,
                     Elster.FIRE_ORANGE]
         }, config));
    }
});

