Ext.define('Mdc.view.setup.comserver.RemoteComServerEdit', {
    extend: 'Ext.window.Window',
    alias: 'widget.remoteComServerEdit',
    title: Uni.I18n.translate('comServer.remoteComserver','MDC','Remote ComServer'),
    width: '80%',
    height: '90%',
    modal: true,
    constrain: true,
    autoShow: true,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    autoScroll: true,
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
                        title: Uni.I18n.translate('general.required','required','Required'),
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
                        title: Uni.I18n.translate('general.optional','MDC','Optional'),
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
                    }
                ]
            }
        ];

        this.buttons = [
            {
                text: Uni.I18n.translate('general.save','MDC','Save'),
                action: 'save'
            },
            {
                text: Uni.I18n.translate('general.cancel','MDC','Cancel'),
                action: 'cancel'
            }
        ];

        this.callParent(arguments);
    }
});
