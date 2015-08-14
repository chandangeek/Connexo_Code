Ext.define('Mdc.view.setup.comserver.ComServerEdit', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Mdc.view.setup.comserver.FieldContainerHelpMessage',
        'Mdc.store.TimeUnitsWithoutMilliseconds'
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
                    itemId: 'txt-communication-server-name',
                    fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                    allowBlank: false,
                    maxLength: 75,
                    required: true,
                    width: 600
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
                    displayField: 'logLevel',
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
                    displayField: 'logLevel',
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
                                allowBlank: false
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
                                    store: 'TimeUnitsWithoutMilliseconds',
                               //     queryMode: 'local',
                                    editable: false,
                                    displayField: 'timeUnit',
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
                                validateOnBlur: false,
                                allowBlank: false
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
                                    store: 'TimeUnitsWithoutMilliseconds',
                           //         queryMode: 'local',
                                    editable: false,
                                    displayField: 'timeUnit',
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
                    xtype: 'numberfield',
                    itemId: 'num-store-task-queue-size',
                    name: 'storeTaskQueueSize',
                    fieldLabel: Uni.I18n.translate('comServer.formFieldLabel.storeTaskQueueSize', 'MDC', 'Store task queue size'),
                    allowBlank: false,
                    minValue: 0,
                    required: true,
                    width: 415
                },
                {
                    xtype: 'numberfield',
                    itemId: 'num-store-task-thread-count',
                    name: 'numberOfStoreTaskThreads',
                    fieldLabel: Uni.I18n.translate('comServer.formFieldLabel.storeTaskThreadCount', 'MDC', 'Store task thread count'),
                    allowBlank: false,
                    minValue: 0,
                    required: true,
                    width: 415
                },
                {
                    xtype: 'numberfield',
                    itemId: 'num-store-task-queue-priority',
                    name: 'storeTaskThreadPriority',
                    fieldLabel: Uni.I18n.translate('comServer.formFieldLabel.storeTaskQueuePriority', 'MDC', 'Store task queue priority'),
                    allowBlank: false,
                    minValue: 0,
                    required: true,
                    width: 415
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
                            href: '#/administration/comservers/'
                        }
                    ]
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
