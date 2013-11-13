Ext.define('Mdc.view.setup.comserver.ComServerEdit', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.comServerEdit',

    layout: 'fit',
    autoShow: true,
    border: 0,

    requires: [
        'Mdc.widget.TimeInfoField'
    ],

    initComponent: function() {
        var loglevels = Ext.create('Mdc.store.LogLevels');

        this.items = [
            {
                xtype: 'form',
                shrinkWrap: 1,
                defaults: {
                  labelWidth: 200
                },
                items: [
                    {
                        xtype: 'textfield',
                        name : 'id',
                        fieldLabel: 'Id'
                    },
                    {
                        xtype: 'textfield',
                        name : 'name',
                        fieldLabel: 'Name'
                    },
                    {
                        xtype: 'textfield',
                        name : 'comServerDescriptor',
                        fieldLabel: 'comServerDescriptor'
                    },
                    {
                        xtype: 'checkbox',
                        name : 'active',
                        inputValue: true,
                        uncheckedValue: 'false',
                        fieldLabel: 'active'
                    },
                    {
                        xtype: 'combobox',
                        name : 'serverLogLevel',
                        fieldLabel: 'Server log level',
                        store: loglevels,
                        queryMode: 'local',
                        displayField: 'logLevel',
                        valueField: 'logLevel'
                    },
                    {
                        xtype: 'combobox',
                        name : 'communicationLogLevel',
                        fieldLabel: 'Communication log level',
                        store: loglevels,
                        queryMode: 'local',
                        displayField: 'logLevel',
                        valueField: 'logLevel'
                    },
                    {
                        xtype: 'timeInfoField',
                        name : 'changesInterPollDelay',
                        fieldLabel: 'changesInterPollDelay '
                    },
                    {
                        xtype: 'timeInfoField',
                        name : 'schedulingInterPollDelay',
                        fieldLabel: 'schedulingInterPollDelay '
                    },
                    {
                        xtype: 'textfield',
                        name : 'queryAPIPostUri',
                        fieldLabel: 'queryAPIPostUri'
                    },
                    {
                        xtype: 'checkbox',
                        inputValue: true,
                        uncheckedValue: 'false',
                        name : 'usesDefaultQueryAPIPostUri',
                        fieldLabel: 'usesDefaultQueryAPIPostUri'
                    },
                    {
                        xtype: 'textfield',
                        name : 'eventRegistrationUri',
                        fieldLabel: 'eventRegistrationUri'
                    },
                    {
                        xtype: 'checkbox',
                        inputValue: true,
                        uncheckedValue: 'false',
                        name : 'usesDefaultEventRegistrationUri',
                        fieldLabel: 'usesDefaultEventRegistrationUri'
                    },
                    {
                        xtype: 'textfield',
                        name : 'storeTaskQueueSize',
                        fieldLabel: 'storeTaskQueueSize'
                    },
                    {
                        xtype: 'textfield',
                        name : 'numberOfStoreTaskThreads',
                        fieldLabel: 'numberOfStoreTaskThreads'
                    },
                    {
                        xtype: 'textfield',
                        name : 'storeTaskThreadPriority',
                        fieldLabel: 'storeTaskThreadPriority'
                    },


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
