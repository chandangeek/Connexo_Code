Ext.define('Mdc.view.setup.comserver.RemoteComServerEdit', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.remoteComServerEdit',
    autoScroll: true,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    border: 0,

    requires: [
        'Mdc.widget.TimeInfoField',
        'Mdc.store.LogLevels',
        'Mdc.view.setup.comport.OutboundComPorts',
        'Mdc.view.setup.comport.InboundComPorts'
    ],

    initComponent: function () {
        var loglevels = Ext.create('Mdc.store.LogLevels');
        var comservers = Ext.create('Mdc.store.ComServers');

        this.items = [
            {
                xtype: 'form',
                shrinkWrap: 1,
                padding: 10,
                border: 0,
                defaults: {
                    labelWidth: 200
                },
                items: [
                    {
                        xtype: 'fieldset',
                        title: 'Required',
                        defaults: {
                            labelWidth: 200
                        },
                        collapsible: true,
                        layout: 'anchor',
                        items: [
                            {
                                xtype: 'textfield',
                                name: 'name',
                                fieldLabel: 'Name'
                            },
                            {
                                xtype: 'textfield',
                                name: 'comServerType',
                                fieldLabel: 'comServerType'
                            },
                            {
                                xtype: 'checkbox',
                                name: 'active',
                                inputValue: true,
                                uncheckedValue: 'false',
                                fieldLabel: 'active'
                            },
                            {
                                xtype: 'combobox',
                                name: 'serverLogLevel',
                                fieldLabel: 'Server log level',
                                store: loglevels,
                                queryMode: 'local',
                                displayField: 'logLevel',
                                valueField: 'logLevel'
                            },
                            {
                                xtype: 'combobox',
                                name: 'communicationLogLevel',
                                fieldLabel: 'Communication log level',
                                store: loglevels,
                                queryMode: 'local',
                                displayField: 'logLevel',
                                valueField: 'logLevel'
                            },
                            {
                                xtype: 'timeInfoField',
                                name: 'changesInterPollDelay',
                                fieldLabel: 'changesInterPollDelay '
                            },
                            {
                                xtype: 'timeInfoField',
                                name: 'schedulingInterPollDelay',
                                fieldLabel: 'schedulingInterPollDelay '
                            },
                            {
                                xtype: 'numberfield',
                                name: 'storeTaskQueueSize',
                                fieldLabel: 'storeTaskQueueSize'
                            },
                            {
                                xtype: 'numberfield',
                                name: 'numberOfStoreTaskThreads',
                                fieldLabel: 'numberOfStoreTaskThreads'
                            },
                            {
                                xtype: 'numberfield',
                                name: 'storeTaskThreadPriority',
                                fieldLabel: 'storeTaskThreadPriority'
                            },
                            {
                                xtype: 'combobox',
                                name: 'onlineComServerId',
                                fieldLabel: 'onlineComServerId',
                                store: comservers,
                                queryMode: 'local',
                                displayField: 'name',
                                valueField: 'id'
                            },
                            {
                                xtype: 'textfield',
                                name: 'queryAPIUsername',
                                fieldLabel: 'queryAPIUsername'
                            },
                            {
                                xtype: 'textfield',
                                inputType:'password',
                                name: 'queryAPIPassword',
                                fieldLabel: 'queryAPIPassword'
                            }
                        ]},
                    {
                        xtype: 'fieldset',
                        title: 'Optional',
                        defaults: {
                            labelWidth: 200
                        },
                        collapsible: true,
                        layout: 'anchor',
                        items: [
                            {
                                xtype: 'textfield',
                                name: 'queryAPIPostUri',
                                fieldLabel: 'queryAPIPostUri'
                            },
                            {
                                xtype: 'checkbox',
                                inputValue: true,
                                uncheckedValue: 'false',
                                name: 'usesDefaultQueryAPIPostUri',
                                fieldLabel: 'usesDefaultQueryAPIPostUri'
                            },
                            {
                                xtype: 'textfield',
                                name: 'eventRegistrationUri',
                                fieldLabel: 'eventRegistrationUri'
                            },
                            {
                                xtype: 'checkbox',
                                inputValue: true,
                                uncheckedValue: 'false',
                                name: 'usesDefaultEventRegistrationUri',
                                fieldLabel: 'usesDefaultEventRegistrationUri'
                            }
                        ]
                    },
                    {"xtype": 'outboundComPorts'},
                    {"xtype": 'inboundComPorts'}
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
