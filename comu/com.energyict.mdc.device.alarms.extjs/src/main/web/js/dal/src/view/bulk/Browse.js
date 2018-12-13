Ext.define('Dal.view.bulk.Browse', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.alarm-bulk-browse',
    itemId: 'alarm-bulk-browse',
    requires: [
        'Uni.view.navigation.SubMenu',
        'Dal.view.bulk.BulkWizard',
        'Dal.view.bulk.Navigation'
    ],

    side: {
        itemId: 'Bulkpanel',
        xtype: 'panel',
        ui: 'medium',
        layout: {
            type: 'vbox',
            align: 'stretch'
        },
        items: [
            {
                itemId: 'bulkNavigation',
                xtype: 'alarm-bulk-navigation'
            }
        ]
    },

    content: [
        {
            xtype: 'alarm-bulk-wizard'
        }
    ]
});