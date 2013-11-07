Ext.define('Mdc.view.setup.ComServerEdit', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.comServerEdit',

    layout: 'fit',
    autoShow: true,
    border: 0,

    stores: [
        'LogLevels'
    ],

    initComponent: function() {
        var loglevels = Ext.create('Mdc.store.LogLevels');

        this.items = [
            {
                xtype: 'form',
                items: [
                    {
                        xtype: 'textfield',
                        name : 'id',
                        fieldLabel: 'Id : '
                    },
                    {
                        xtype: 'textfield',
                        name : 'name',
                        fieldLabel: 'Name : '
                    },
                    {
                        xtype: 'textfield',
                        name : 'active',
                        fieldLabel: 'active : '
                    },
                    {
                        xtype: 'combobox',
                        name : 'serverLogLevel',
                        fieldLabel: 'Server log level : ',
                        store: loglevels,
                        queryMode: 'local',
                        displayField: 'level',
                        valueField: 'level'
                    },
                    {
                        xtype: 'combobox',
                        name : 'communicationLogLevel',
                        fieldLabel: 'Communication log level : ',
                        store: loglevels,
                        queryMode: 'local',
                        displayField: 'level',
                        valueField: 'level'
                    }
                ]
            }
        ];

        this.buttons = [
            {
                text: 'Save',
                action: 'save'
            },
            {
                text: 'Cancel',
                action: 'cancel'
            }
        ];

        this.callParent(arguments);
    }
});
