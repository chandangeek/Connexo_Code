Ext.define('Idc.view.workspace.issues.bulk.Browse', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.bulk-browse',
    itemId: 'bulk-browse',
    componentCls: 'isu-bulk-browse',
    requires: [
        'Uni.view.navigation.SubMenu',
        'Idc.view.workspace.issues.bulk.BulkWizard',
        'Idc.view.workspace.issues.bulk.Navigation'
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