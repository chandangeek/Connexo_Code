Ext.define('Itk.view.bulk.Browse', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.issue-bulk-browse',
    itemId: 'issue-bulk-browse',
    requires: [
        'Uni.view.navigation.SubMenu',
        'Itk.view.bulk.BulkWizard',
        'Itk.view.bulk.Navigation'
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
                xtype: 'issue-bulk-navigation'
            }
        ]
    },

    content: [
        {
            xtype: 'issue-bulk-wizard'
        }
    ]
});