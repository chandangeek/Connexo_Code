Ext.define('Imt.metrologyconfiguration.view.GeneralPreview', {
    extend: 'Ext.panel.Panel',
    itemId: 'metrologyConfigurationGeneralPreview',

    requires: [
        'Imt.metrologyconfiguration.view.ActionMenu'
    ],

    frame: true,

    initComponent: function () {
        var me = this;

        me.callParent(arguments);
    }
});


