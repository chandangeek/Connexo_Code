Ext.define('Skyline.panel.Panel', {
    override: 'Ext.panel.Panel',

    panelToolbarConfig: {
        headerPosition: 'left'
    },

    constructor: function(config) {
        if ('toolbar' == config.ui) {
            Ext.applyIf(config, this.panelToolbarConfig);
            Ext.apply(this, config);
        }
        this.callSuper([config]);
    }
});