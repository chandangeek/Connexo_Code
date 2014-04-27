Ext.define('Isu.view.workspace.issues.Browse', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.issues-browse',
    ui: 'large',
    title: 'Issues',

    items: [
        {
            xtype: 'issues-filter'
        },
        {
            xtype: 'issue-no-group'
        },
        {
            xtype: 'issues-list'
        },
        {
            xtype: 'issues-item'
        }
    ]
});