Ext.define('Isu.view.administration.communicationtasks.Item', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Isu.view.ext.button.ItemAction',
        'Isu.view.administration.communicationtasks.ActionMenu',
        'Isu.view.administration.communicationtasks.Form'
    ],
    alias: 'widget.communication-tasks-item',
    height: 310,
    title: 'Details',
    frame: true,
    tools: [
        {
            xtype: 'item-action',
            menu: {
                xtype: 'communication-tasks-action-menu',
                issueId: this.record ? this.record.getId() : null
            }
        }
    ],
    items: {
        xtype: 'communication-tasks-form'
        // todo: animate button
//        buttons: [{
//            text: 'View details',
//            action: 'view'
//        }]
    },
    // todo: set empty text
    emptyText: '<h3>No task selected</h3><p>Select a task to view its detail.</p>'
});