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
    itemId : 'issues-item',
    frame: true,
    tools: [
        {
            text: 'Actions',
            itemId: 'item-action',
            xtype: 'item-action',
            menu: {
                itemId: 'action-menu',
                xtype: 'issue-action-menu',
                issueId: this.record ? this.record.getId() : null
            }
        }
    ],
    items: {
        itemId: 'issue-form',
        xtype: 'issue-form',
        // todo: animate button
        buttons: [{
            text: 'View details',
            ui: 'link',
            action: 'view'
            //listeners: {
            //    click: function() {
            //        console.log(Ext.ComponentQuery.query('#action-menu')[0]);
            //        window.location.href = "#/workspace/datacollection/issues/" + Ext.ComponentQuery.query('#item-action')[0].issueId
            //    }
            //}
        }]
    },
    // todo: set empty text
    emptyText: '<h3>No issue selected</h3><p>Select an issue to view its detail.</p>'
});