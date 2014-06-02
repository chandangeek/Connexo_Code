Ext.define('Isu.view.workspace.issues.Item', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Isu.view.ext.button.ItemAction',
        'Isu.view.workspace.issues.ActionMenu',
        'Isu.view.workspace.issues.Form'
    ],
    alias: 'widget.issues-item',
    title: 'Details',
    itemId: 'issues-item',
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
        bbar: {
            layout: {
                type: 'vbox',
                align: 'right'
            },
            items: {
                text: 'View details',
                itemId: 'viewDetails',
                ui: 'link',
                action: 'view',
                listeners: {
                    click: function () {
                        window.location.href = "#/workspace/datacollection/issues/" + Ext.ComponentQuery.query('#issue-form')[0].getRecord().get('id')
                    }
                }
            }
        }

    },

// todo: set empty text
    emptyText: '<h3>No issue selected</h3><p>Select an issue to view its detail.</p>'

});