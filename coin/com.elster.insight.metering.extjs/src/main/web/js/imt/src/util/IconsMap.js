/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.util.IconsMap', {
    singleton: true,

    icons: {
        ELECTRICITY: 'icon-power',
        GAS: 'icon-fire2',
        WATER: 'icon-droplet',
        HEAT: 'icon-fire',
        connected: 'icon-link4',
        physicallyDisconnected: 'icon-unlink3',
        logicallyDisconnected: 'icon-unlink',
        demolished: 'icon-cancel-circle',
        underConstruction: 'icon-construction'
    },

    getCls: function (key) {
        return this.icons[key];
    }
});