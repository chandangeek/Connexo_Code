Ext.define('Dcs.view.admin.Administration', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.administration',
    itemId: 'administration',
    requires: [
    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    items: [
        {
            xtype: 'component',
            html: '<h3><a style="font-family:VAGRoundedStdLight,Arial,Helvetica,Sans-Serif;color:#007dc3" href="#administration_dcs/scheduling">Data Collection Schedules</a></h3>',
            margin: '30 30 30 30'
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});
