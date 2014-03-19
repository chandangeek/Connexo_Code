Ext.define('Isu.view.workspace.issues.bulk.Navigation', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.bulk-navigation',
    componentCls: 'isu-bulk-navigation',
    layout: 'vbox',
    defaults: {
        width: 120,
        textAlign: 'left'
    },


    items: [
        {
            xtype: 'button',
            name: 'select-issues',
            number: 0,
            text: '1. Select issues',
            renderTo: Ext.getBody()
        },
        {
            xtype: 'button',
            name: 'select-action',
            number: 1,
            disabled: true,
            text: '2. Select action',
            renderTo: Ext.getBody()
        },
        {
            xtype: 'button',
            name: 'action-details',
            number: 2,
            disabled: true,
            text: '3. Action details',
            renderTo: Ext.getBody()
        },
        {
            xtype: 'button',
            name: 'confirmation',
            number: 3,
            disabled: true,
            text: '4. Confirmation',
            renderTo: Ext.getBody()
        },
        {
            xtype: 'button',
            name: 'status',
            number: 4,
            disabled: true,
            text: '5. Status',
            renderTo: Ext.getBody()
        }
    ]

});