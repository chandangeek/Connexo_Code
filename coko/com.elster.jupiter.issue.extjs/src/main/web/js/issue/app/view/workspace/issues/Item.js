Ext.define('Isu.view.workspace.issues.Item', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Isu.view.ext.button.ItemAction',
        'Isu.view.workspace.issues.ActionMenu',
        'Isu.view.workspace.issues.Form',
        'Isu.view.workspace.issues.Actions'
    ],
    alias: 'widget.issues-item',
    title: 'Details',
    frame: true,
    tools: [
        {
            xtype: 'item-action',
            menu: {
                xtype: 'issue-action-menu',
                issueId: this.record ? this.record.getId() : null
            }
        }
    ],
    items: {
        xtype: 'issue-form'
        // todo: animate button
//        buttons: [{
//            text: 'View details',
//            action: 'view'
//        }]
    },
    // todo: set empty text
    emptyText: '<h3>No issue selected</h3><p>Select an issue to view its detail.</p>'
});