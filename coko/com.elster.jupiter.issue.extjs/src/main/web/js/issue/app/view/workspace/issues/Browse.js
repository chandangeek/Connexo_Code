Ext.define('Isu.view.workspace.issues.Browse', {
    itemId: 'Panel',
    extend: 'Ext.panel.Panel',
    alias: 'widget.issues-browse',
    ui: 'large',
    title: 'Issues',

    items: [
        {   itemId: 'issues-filter',
            xtype: 'issues-filter'
        },
        {   itemId: 'issues-no-group',
            xtype: 'issue-no-group'
        },
        {
            xtype: 'preview-container',
            grid: {
                xtype: 'issues-list'
            },
            emptyComponent: {
                itemId: 'noIssues',
                name: 'noIssues',
                hidden: true
            },
            previewComponent: {
                xtype: 'issues-item'
            }
        }
    ]
});