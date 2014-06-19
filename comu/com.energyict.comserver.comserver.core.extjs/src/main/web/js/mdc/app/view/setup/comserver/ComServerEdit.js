/*
 Ext.define('Mdc.view.setup.comserver.ComServerEdit', {
 extend: 'Ext.window.Window',
 alias: 'widget.comServerEdit',
 autoScroll: true,
 title: 'ComServer',
 width: '80%',
 height: '90%',
 modal: true,
 constrain: true,
 autoShow: true,
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
 fieldLabel: 'storeTaskQueueSize',
 minValue: 0
 },
 {
 xtype: 'numberfield',
 name: 'numberOfStoreTaskThreads',
 fieldLabel: 'numberOfStoreTaskThreads',
 minValue: 0
 },
 {
 xtype: 'numberfield',
 name: 'storeTaskThreadPriority',
 fieldLabel: 'storeTaskThreadPriority',
 minValue: 0
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
 */

Ext.define('Mdc.view.setup.comserver.ComServerEdit', {
    extend: 'Uni.view.container.ContentContainer',
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
                    anchor: '100%'
                },
                {
                    xtype: 'displayfield',
                    name: 'comServerTypeVisual',
                    fieldLabel: Uni.I18n.translate('comServer.formFieldLabel.serverType', 'MDC', 'Server type'),
                    anchor: '100%',
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
                    anchor: '100%'
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
                    anchor: '100%'
                },
                {
                    xtype: 'fieldcontainer',
                    columnWidth: 0.5,
                    fieldLabel: Uni.I18n.translate('comServer.formFieldLabel.changesInterPollDelay', 'MDC', 'Changes inter poll delay'),
                    afterSubTpl: Uni.I18n.translate('comServer.formFieldNote.minimalAcceptableDelayIs60Seconds', 'MDC', 'The minimal acceptable delay is 60 seconds'),
                    required: true,
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
                    xtype: 'fieldcontainer',
                    columnWidth: 0.5,
                    fieldLabel: Uni.I18n.translate('comServer.formFieldLabel.schedulingInterPollDelay', 'MDC', 'Scheduling inter poll delay'),
                    afterSubTpl: Uni.I18n.translate('comServer.formFieldNote.minimalAcceptableDelayIs60Seconds', 'MDC', 'The minimal acceptable delay is 60 seconds'),
                    required: true,
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
