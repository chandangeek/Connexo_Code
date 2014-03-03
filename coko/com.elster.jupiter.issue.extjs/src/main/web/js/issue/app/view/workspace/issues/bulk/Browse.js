Ext.define('Mtr.view.workspace.issues.bulk.Browse', {
    extend: 'Ext.container.Container',
    alias: 'widget.bulk-browse',
    requires: [
        'Mtr.view.workspace.issues.bulk.Navigation',
        'Mtr.view.workspace.issues.bulk.BulkWizard',
        'Uni.view.breadcrumb.Trail'
    ],
    layout: 'border',
    items: [
        {
            xtype: 'breadcrumbTrail',
            region: 'north',
            padding: 6
        },
        {
            xtype: 'bulk-navigation',
            region: 'west'
        },
        {
            xtype: 'bulk-wizard',
            region: 'center'
        }
    ]
});