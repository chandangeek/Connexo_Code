Ext.define('Mtr.view.workspace.issues.bulk.Browse', {
    extend: 'Ext.container.Container',
    alias: 'widget.bulk-browse',
    requires: [
        'Mtr.view.workspace.issues.bulk.Navigation',
        'Mtr.view.workspace.issues.bulk.BulkWizard',
        'Uni.view.breadcrumb.Trail'
    ],
    layout: 'border',
    overflowY: 'auto',
    items: [
        {
            xtype: 'breadcrumbTrail',
            region: 'north',
            padding: 6
        },
        {
            xtype: 'bulk-navigation',
            region: 'west',
            margin: '0 10',
            width: 150
        },
        {
            xtype: 'container',
            style: {
                backgroundColor: '#fff'
            },
            region: 'center',
            items: {
                xtype: 'bulk-wizard'
            }

        }
    ]
});