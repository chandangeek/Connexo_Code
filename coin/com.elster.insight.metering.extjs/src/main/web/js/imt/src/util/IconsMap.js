Ext.define('Imt.util.IconsMap', {
    singleton: true,

    icons: {
        ELECTRICITY: 'icon-power',
        GAS: 'icon-fire2',
        WATER: 'icon-droplet',
        HEAT: 'icon-rating3',
        CONNECTED: 'icon-rating3',
        PHYSICALLY_DISCONNECTED: 'icon-rating3',
        LOGICALLY_DISCONNECTED: 'icon-rating3',
        DEMOLISHED: 'icon-blocked',
        UNDER_CONSTRUCTION: 'icon-construction'
    },

    getCls: function (key) {
        return this.icons[key];
    }
});