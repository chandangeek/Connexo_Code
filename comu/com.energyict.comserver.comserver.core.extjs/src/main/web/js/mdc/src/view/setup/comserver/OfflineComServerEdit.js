/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comserver.OfflineComServerEdit', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Mdc.view.setup.comserver.FieldContainerHelpMessage',
        'Mdc.store.TimeUnitsWithoutMilliseconds',
        'Mdc.store.ComServers',
        'Uni.util.FormErrorMessage'
    ],
    alias: 'widget.offlineComServerEdit',
    itemId: 'offlineComServerEdit',
    edit: false,

    content: [
        {
            xtype: 'form',
            itemId: 'offlineComServerEditForm',
            ui: 'large',
            width: '100%',
            defaults: {
                labelWidth: 250,
                validateOnChange: false,
                validateOnBlur: false
            },
            items: [
                {
                    itemId: 'form-errors',
                    xtype: 'uni-form-error-message',
                    name: 'form-errors',
                    hidden: true,
                    width: 600
                },
                {
                    xtype: 'hiddenfield',
                    name: 'id'
                },
                {
                    xtype: 'hiddenfield',
                    name: 'comServerType'
                },
                {
                    xtype: 'textfield',
                    name: 'name',
                    itemId: 'txt-communication-server-name',
                    fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                    maxLength: 75,
                    required: true,
                    width: 600,
                    listeners: {
                        afterrender: function(field) {
                            field.focus(false, 200);
                        }
                    }
                },
                {
                    xtype: 'displayfield',
                    name: 'comServerTypeVisual',
                    itemId: 'fld-server-type',
                    fieldLabel: Uni.I18n.translate('comServer.formFieldLabel.serverType', 'MDC', 'Server type'),
                    width: 600,
                    hidden: true
                },
                {
                    xtype: 'combobox',
                    name: 'serverLogLevel',
                    itemId: 'cbo-server-log-level',
                    fieldLabel: Uni.I18n.translate('comServer.formFieldLabel.serverLogLevel', 'MDC', 'Server log level'),
                    store: 'Mdc.store.LogLevels',
                    queryMode: 'local',
                    editable: false,
                    displayField: 'localizedValue',
                    valueField: 'logLevel',
                    allowBlank: false,
                    required: true,
                    width: 600

                },
                {
                    xtype: 'combobox',
                    name: 'communicationLogLevel',
                    itemId: 'cbo-communication-log-level',
                    fieldLabel: Uni.I18n.translate('comServer.formFieldLabel.communicationLogLevel', 'MDC', 'Communication log level'),
                    store: 'Mdc.store.LogLevels',
                    queryMode: 'local',
                    editable: false,
                    displayField: 'localizedValue',
                    valueField: 'logLevel',
                    allowBlank: false,
                    required: true,
                    width: 600
                },
                {
                    xtype: 'fieldcontainer',
                    itemId: 'fld-changes-inter-poll-delay',
                    columnWidth: 0.5,
                    fieldLabel: Uni.I18n.translate('comServer.formFieldLabel.changesInterPollDelay', 'MDC', 'Changes inter poll delay'),
                    required: true,
                    width: 600,
                    items: [
                        {
                            xtype: 'container',
                            layout: 'hbox',
                            defaults: {
                                validateOnChange: false,
                                validateOnBlur: false,
                            },
                            items: [
                                {
                                    xtype: 'numberfield',
                                    itemId: 'num-changes-inter-poll-delay',
                                    name: 'changesInterPollDelay[count]',
                                    width: 150,
                                    margin: '0 10 0 0',
                                    minValue: 0
                                },
                                {
                                    xtype: 'combobox',
                                    name: 'changesInterPollDelay[timeUnit]',
                                    itemId: 'cbo-changes-inter-poll-delay',
                                    store: 'Mdc.store.TimeUnitsWithoutMilliseconds',
                                    queryMode: 'local',
                                    editable: false,
                                    displayField: 'localizedValue',
                                    valueField: 'timeUnit',
                                    flex: 1
                                }
                            ]
                        },
                        {
                            xtype: 'fieldContainerHelpMessage',
                            itemId: 'msg-changes-inter-poll-delay',
                            text: Uni.I18n.translate('comServer.formFieldNote.minimalAcceptableDelayIs60Seconds', 'MDC', 'The minimal acceptable delay is 60 seconds')
                        }
                    ]
                },
                {
                    xtype: 'fieldcontainer',
                    itemId: 'fld-scheduling-inter-poll-delay',
                    columnWidth: 0.5,
                    fieldLabel: Uni.I18n.translate('comServer.formFieldLabel.schedulingInterPollDelay', 'MDC', 'Scheduling inter poll delay'),
                    required: true,
                    width: 600,
                    items: [
                        {
                            xtype: 'container',
                            layout: 'hbox',
                            defaults: {
                                validateOnChange: false,
                                validateOnBlur: false
                            },
                            items: [
                                {
                                    xtype: 'numberfield',
                                    itemId: 'num-scheduling-inter-poll-delay',
                                    name: 'schedulingInterPollDelay[count]',
                                    width: 150,
                                    margin: '0 10 0 0',
                                    minValue: 0
                                },
                                {
                                    xtype: 'combobox',
                                    itemId: 'cbo-scheduling-inter-poll-delay',
                                    name: 'schedulingInterPollDelay[timeUnit]',
                                    store: 'Mdc.store.TimeUnitsWithoutMilliseconds',
                                    queryMode: 'local',
                                    editable: false,
                                    displayField: 'localizedValue',
                                    valueField: 'timeUnit',
                                    flex: 1
                                }
                            ]
                        },
                        {
                            xtype: 'fieldContainerHelpMessage',
                            itemId: 'msg-scheduling-inter-poll-delay',
                            text: Uni.I18n.translate('comServer.formFieldNote.minimalAcceptableDelayIs60Seconds', 'MDC', 'The minimal acceptable delay is 60 seconds')
                        }
                    ]
                },
                {
                    xtype: 'textfield',
                    itemId: 'txt-communication-server-server-name',
                    fieldLabel: Uni.I18n.translate('comserver.formFieldLabel.server.serverName', 'MDC', 'Server name'),
                    name: 'serverName',
                    width: 600,
                    required: true,
                    blankText:Uni.I18n.translate('general.required.field', 'MDC', 'This field is required')
                },
                {
                    xtype: 'combobox',
                    name: 'onlineComServerId',
                    fieldLabel: Uni.I18n.translate('comserver.formFieldLabel.server.onlineServer', 'MDC', 'Online ComServer'),
                    store: 'Mdc.store.ComServers',
                    queryMode: 'local',
                    displayField: 'name',
                    valueField: 'id'
                },
                {
                    xtype: 'fieldcontainer',
                    ui: 'actions',
                    fieldLabel: '&nbsp',
                    items: [
                        {
                            text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                            xtype: 'button',
                            ui: 'action',
                            action: 'saveModel',
                            itemId: 'createEditButton'
                        },
                        {
                            text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                            xtype: 'button',
                            ui: 'link',
                            itemId: 'cancelLink',
                            href: '#/administration/offlinecomservers/'
                        }
                    ]
                }
            ]
        }
    ],

    initComponent: function() {
        this.callParent(arguments);
        var nameField = this.down('#offlineComServerEditForm').getForm().findField('name');
        nameField.on({
            change: {fn: this.setServerName, scope: this}}
        );
    },

    isEdit: function () {
        return this.edit;
    },

    setEdit: function (edit, returnLink) {
        if (edit) {
            this.edit = edit;
            this.down('#createEditButton').setText(Uni.I18n.translate('general.save', 'MDC', 'Save'));
        } else {
            this.edit = edit;
            this.down('#createEditButton').setText(Uni.I18n.translate('general.add', 'MDC', 'Add'));
        }
        this.down('#cancelLink').href = returnLink;
    },

    setServerName: function(){
        if (!this.isEdit()){
            var nameField = this.down('#offlineComServerEditForm').getForm().findField('name'),
                serverNameField = this.down('#offlineComServerEditForm').getForm().findField('serverName'),
                name = nameField.getValue();
            serverNameField.setValue(name);
        }
    }

});
