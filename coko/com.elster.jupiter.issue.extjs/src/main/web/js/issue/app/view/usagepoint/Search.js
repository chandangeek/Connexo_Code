Ext.define('Mtr.view.usagepoint.Search', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.usagePointSearch',
    title: 'Search usagePoint',
    initComponent: function () {
        this.items = [
            {
                xtype: 'form',
                items: [
                    {
                        xtype: 'textfield',
                        name: 'mRID',
                        fieldLabel: 'Master Resource Identifier'
                    },
                    {
                        xtype: 'textfield',
                        name: 'name',
                        fieldLabel: 'Name'
                    },
                    {
                        xtype: 'textfield',
                        name: 'aliasName',
                        fieldLabel: 'aliasName'
                    },
                    {
                        xtype: 'textfield',
                        name: 'ratedPower.value',
                        fieldLabel: 'rated Power (kWh)'
                    },
                    {
                        xtype: 'textfield',
                        name: 'connectionState',
                        fieldLabel: 'connection state'
                    },
                    {
                        xtype: 'checkbox',
                        name: 'checkBilling',
                        fieldLabel: 'check billing'
                    },
                    {
                        xtype: 'textfield',
                        name: 'userName',
                        fieldLabel: 'user'
                    }
                ]
            }
        ];
        this.buttons = [
            {
                text: 'Search',
                action: 'search'
            }
        ];
        this.callParent(arguments);
    }
});

