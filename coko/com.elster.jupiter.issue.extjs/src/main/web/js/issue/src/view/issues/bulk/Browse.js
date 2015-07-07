Ext.define('Isu.view.issues.bulk.Browse', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.bulk-browse',
    itemId: 'bulk-browse',
    componentCls: 'isu-bulk-browse',
    requires: [
        'Uni.view.navigation.SubMenu',
        'Isu.view.issues.bulk.BulkWizard',
        'Isu.view.issues.bulk.Navigation'
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
                xtype: 'bulk-navigation'
            }
        ]
    },

    content: [
        {
            xtype: 'bulk-wizard'
        }
    ]
});