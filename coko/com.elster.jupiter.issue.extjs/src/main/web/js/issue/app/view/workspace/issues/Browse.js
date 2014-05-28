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
        {   itemId: 'issues-list',
            xtype: 'issues-list'
        },
        {   itemId: 'issues-item',
            xtype: 'issues-item'
        }
    ]
});