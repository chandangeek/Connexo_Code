Ext.define('Isu.view.workspace.issues.bulk.Navigation', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.bulk-navigation',
    componentCls: 'isu-bulk-navigation',
    layout: 'vbox',

    /*defaults: {
        width: 120,
        textAlign: 'left'
    },*/


    items: [
        {
      //      xtype: 'step-panel',
            text: '10'
        //    baseCls: 'isu-bulk-navigation-btns',
        //    disabledCls: 'disabled-bulk-navigation-btns',
        //    componentCls: 'active-bulk-list-action',
        //    name: 'select-issues',
        //    number: 0,
        //   disabled: true,
        //    text: '1',
        //    renderTo: Ext.getBody()
        },
        {
            xtype: 'button',
            baseCls: 'isu-bulk-navigation-btns',
            disabledCls: 'disabled-bulk-navigation-btns',

            name: 'select-action',
            number: 1,
            disabled: true,
            text: '2- Select action',
            renderTo: Ext.getBody()
        },
        {
            xtype: 'button',
            baseCls: 'isu-bulk-navigation-btns',
            disabledCls: 'disabled-bulk-navigation-btns',
            name: 'action-details',
            number: 2,
            disabled: true,
            text: '3- Action details',
            renderTo: Ext.getBody()
        },
        {
            xtype: 'button',
            baseCls: 'isu-bulk-navigation-btns',
            disabledCls: 'disabled-bulk-navigation-btns',
            name: 'confirmation',
            number: 3,
            disabled: true,
            text: '4- Confirmation',
            renderTo: Ext.getBody()
        },
        {
            xtype: 'button',
            baseCls: 'isu-bulk-navigation-btns',
            disabledCls: 'disabled-bulk-navigation-btns',
            name: 'status',
            number: 4,
            disabled: true,
            text: '5- Status',
            renderTo: Ext.getBody()
        }
    ]

});