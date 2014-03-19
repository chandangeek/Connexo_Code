Ext.define('Isu.view.workspace.issues.bulk.Browse', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.bulk-browse',
    componentCls: 'isu-bulk-browse',
    requires: [
        'Isu.view.workspace.issues.bulk.BulkWizard',
        'Isu.view.workspace.issues.bulk.Navigation'
    ],

    side: [
        {
            xtype: 'bulk-navigation'
        }
    ],

    content: [
        {
            xtype: 'bulk-wizard',
            defaults: {
                cls: 'content-wrapper'
            }
        }
    ]
});