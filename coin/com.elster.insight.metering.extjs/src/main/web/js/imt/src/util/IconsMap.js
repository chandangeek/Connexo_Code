Ext.define('Imt.util.IconsMap', {
    singleton: true,

    icons: {
        ELECTRICITY: 'icon-power',
        GAS: 'icon-fire2',
        WATER: 'icon-droplet',
        HEAT: 'icon-rating3',
        CONNECTED: 'icon-rating3',
        PHYSICALLYDISCONNECTED: 'icon-rating3',
        LOGICALLYDISCONNECTED: 'icon-rating3',
        UNKNOWN: 'icon-blocked',
        UNDERCONSTRUCTION: 'icon-construction'
    },

    getCls: function (key) {
        return this.icons[key];
    }
});