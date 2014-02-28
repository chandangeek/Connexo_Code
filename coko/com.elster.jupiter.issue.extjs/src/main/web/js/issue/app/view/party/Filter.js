Ext.define('Mtr.view.party.Filter', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.partyFilter',
    title: 'Filter',
    cls: 'filter-form',

    items: [
        {
            xtype: 'form',
            items: [
                {
                    xtype: 'textfield',
                    name: 'mRID',
                    fieldLabel: 'MRID'
                },
                {
                    xtype: 'textfield',
                    name: 'name',
                    fieldLabel: 'Name'
                },
                {
                    xtype: 'textfield',
                    name: 'aliasName',
                    fieldLabel: 'Alias'
                },
                {
                    xtype: 'textfield',
                    name: 'description',
                    fieldLabel: 'Description'
                }
            ]
        }
    ],

    buttons: [
        {
            text: 'Apply',
            action: 'filter'
        },
        {
            text: 'Reset',
            action: 'reset'
        }
    ],


    initComponent: function () {
        this.callParent(arguments);
    }
});