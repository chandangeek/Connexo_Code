Ext.define('Isu.view.workspace.issues.bulk.Browse', {
    extend: 'Ext.container.Container',
    alias: 'widget.bulk-browse',
    requires: [
        'Isu.view.workspace.issues.bulk.Navigation',
        'Isu.view.workspace.issues.bulk.BulkWizard',
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
            region: 'center',
            style: {
                backgroundColor: '#fff'
            },
            items: {
                xtype: 'bulk-wizard',
                defaults: {
                    cls: 'content-wrapper'
                }
            }

        }
    ]
});