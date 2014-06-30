Ext.define('Mdc.view.setup.comserver.ComServerEdit', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Mdc.view.setup.comserver.FieldContainerHelpMessage'
    ],
    alias: 'widget.comServerEdit',
    itemId: 'comServerEdit',

    edit: false,

    content: [
        {
            xtype: 'form',
            itemId: 'comServerEditForm',
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
                    hidden: true
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
                    fieldLabel: Uni.I18n.translate('general.formFieldLabel.name', 'MDC', 'Name'),
                    allowBlank: false,
                    required: true,
                    width: 600
                },
                {
                    xtype: 'displayfield',
                    name: 'comServerTypeVisual',
                    fieldLabel: Uni.I18n.translate('comServer.formFieldLabel.serverType', 'MDC', 'Server type'),
                    width: 600,
                    hidden: true
                },
                {
                    xtype: 'combobox',
                    name: 'serverLogLevel',
                    fieldLabel: Uni.I18n.translate('comServer.formFieldLabel.serverLogLevel', 'MDC', 'Server log level'),
                    store: 'Mdc.store.LogLevels',
                    queryMode: 'local',
                    editable: false,
                    displayField: 'logLevel',
                    valueField: 'logLevel',
                    allowBlank: false,
                    required: true,
                    width: 600

                },
                {
                    xtype: 'combobox',
                    name: 'communicationLogLevel',
                    fieldLabel: Uni.I18n.translate('comServer.formFieldLabel.communicationLogLevel', 'MDC', 'Communication log level'),
                    store: 'Mdc.store.LogLevels',
                    queryMode: 'local',
                    editable: false,
                    displayField: 'logLevel',
                    valueField: 'logLevel',
                    allowBlank: false,
                    required: true,
                    width: 600
                },
                {
                    xtype: 'fieldcontainer',
                    columnWidth: 0.5,
                    fieldLabel: Uni.I18n.translate('comServer.formFieldLabel.changesInterPollDelay', 'MDC', 'Changes inter poll delay'),
                    required: true,
                    width: 600,
                    items: [
                        {
                            xtype: 'container',
                            layout: {
                                type: 'hbox',
                                align: 'stretch'
                            },
                            defaults: {
                                validateOnChange: false,
                                validateOnBlur: false,
                                allowBlank: false
                            },
                            items: [
                                {
                                    xtype: 'textfield',
                                    name: 'changesInterPollDelay[count]',
                                    width: 150,
                                    margin: '0 10 0 0'
                                },
                                {
                                    xtype: 'combobox',
                                    name: 'changesInterPollDelay[timeUnit]',
                                    store: 'Mdc.store.TimeUnitsWithoutMilliseconds',
                                    queryMode: 'local',
                                    editable: false,
                                    displayField: 'timeUnit',
                                    valueField: 'timeUnit',
                                    flex: 1
                                }
                            ]
                        },
                        {
                            xtype: 'fieldContainerHelpMessage',
                            text: Uni.I18n.translate('comServer.formFieldNote.minimalAcceptableDelayIs60Seconds', 'MDC', 'The minimal acceptable delay is 60 seconds')
                        }
                    ]
                },
                {
                    xtype: 'fieldcontainer',
                    columnWidth: 0.5,
                    fieldLabel: Uni.I18n.translate('comServer.formFieldLabel.schedulingInterPollDelay', 'MDC', 'Scheduling inter poll delay'),
                    required: true,
                    width: 600,
                    items: [
                        {
                            xtype: 'container',
                            layout: {
                                type: 'hbox',
                                align: 'stretch'
                            },
                            defaults: {
                                validateOnChange: false,
                                validateOnBlur: false,
                                allowBlank: false
                            },
                            items: [
                                {
                                    xtype: 'textfield',
                                    name: 'schedulingInterPollDelay[count]',
                                    width: 150,
                                    margin: '0 10 0 0'
                                },
                                {
                                    xtype: 'combobox',
                                    name: 'schedulingInterPollDelay[timeUnit]',
                                    store: 'Mdc.store.TimeUnitsWithoutMilliseconds',
                                    queryMode: 'local',
                                    editable: false,
                                    displayField: 'timeUnit',
                                    valueField: 'timeUnit',
                                    flex: 1
                                }
                            ]
                        },
                        {
                            xtype: 'fieldContainerHelpMessage',
                            text: Uni.I18n.translate('comServer.formFieldNote.minimalAcceptableDelayIs60Seconds', 'MDC', 'The minimal acceptable delay is 60 seconds')
                        }
                    ]
                },
                {
                    xtype: 'numberfield',
                    name: 'storeTaskQueueSize',
                    fieldLabel: Uni.I18n.translate('comServer.formFieldLabel.storeTaskQueueSize', 'MDC', 'Store task queue size'),
                    allowBlank: false,
                    minValue: 0,
                    required: true,
                    width: 415
                },
                {
                    xtype: 'numberfield',
                    name: 'numberOfStoreTaskThreads',
                    fieldLabel: Uni.I18n.translate('comServer.formFieldLabel.storeTaskThreadCount', 'MDC', 'Store task thread count'),
                    allowBlank: false,
                    minValue: 0,
                    required: true,
                    width: 415
                },
                {
                    xtype: 'numberfield',
                    name: 'storeTaskThreadPriority',
                    fieldLabel: Uni.I18n.translate('comServer.formFieldLabel.storeTaskQueuePriority', 'MDC', 'Store task queue priority'),
                    allowBlank: false,
                    minValue: 0,
                    required: true,
                    width: 415
                }
            ],
            buttons: [
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
                    href: '#/administration/comservers/'
                }
            ]
        }
    ],

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
    }
});
