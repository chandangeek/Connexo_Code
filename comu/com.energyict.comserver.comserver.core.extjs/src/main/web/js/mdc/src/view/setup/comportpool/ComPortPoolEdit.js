/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comportpool.ComPortPoolEdit', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.property.form.Property',
    ],
    alias: 'widget.comPortPoolEdit',
    itemId: 'comPortPoolEdit',

    edit: false,

    initComponent: function() {
        var me = this;
        me.content = [
        {
            xtype: 'form',
            itemId: 'comPortPoolEditForm',
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
                    width: 600,
                    hidden: true
                },
                {
                    xtype: 'hiddenfield',
                    name: 'id'
                },
                {
                    xtype: 'hiddenfield',
                    name: 'direction'
                },
                {
                    xtype: 'textfield',
                    name: 'name',
                    itemId: 'txt-comportpool-name',
                    fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                    width: 600,
                    required: true,
                    listeners: {
                        afterrender: function(field) {
                            field.focus(false, 200);
                        }
                    }
                },
                {
                    xtype: 'displayfield',
                    name: 'direction_visual',
                    itemId: 'txt-comportpool-direction',
                    fieldLabel: Uni.I18n.translate('comports.preview.direction', 'MDC', 'Direction'),
                    hidden: true,
                    width: 600
                },
                {
                    xtype: 'combobox',
                    name: 'comPortType',
                    itemId: 'cbo-comportpool-type',
                    fieldLabel: Uni.I18n.translate('general.type', 'MDC', 'Type'),
                    emptyText: Uni.I18n.translate('comPortPool.formFieldEmptyText.selectCommunicationType', 'MDC', 'Select communication type...'),
                    store: 'Mdc.store.ComPortTypes',
                    queryMode: 'local',
                    editable: false,
                    displayField: 'localizedValue',
                    valueField: 'id',
                    required: true,
                    width: 600
                },
                {
                    xtype: 'numberfield',
                    name: 'pctHighPrioTasks',
                    fieldLabel: Uni.I18n.translate('comPortPool.form.percentageOfPriorityTasks', 'MDC', 'Percentage of priority tasks'),
                    itemId: 'percentageOfPriorityTasks',
                    value: 0,
                    maxValue: 100,
                    minValue: 0,
                    required: true,
                    allowBlank: false,
                    listeners: {
                        change: function () {
                            me.changeMaxPriorityConnections();
                        },
                        blur: me.numberFieldValidation
                    }
                },
                {
                    xtype: 'displayfield',
                    name: 'maxPriorityConnections',
                    fieldLabel: Uni.I18n.translate('comPortPool.form.maxPriorityConnections', 'MDC', 'Max priority connections'),
                    itemId: 'maxPriorityConnections',
                    editable: false,
                    hidden: true,
                    listeners: {
                        afterrender: function (field) {
                            field.focus(false, 500);
                        }
                    }
                },
                {
                    xtype: 'combobox',
                    name: 'discoveryProtocolPluggableClassId',
                    itemId: 'cbo-comportpool-protocol-detect',
                    fieldLabel: Uni.I18n.translate('comPortPool.formFieldLabel.protocolDetection', 'MDC', 'Protocol detection'),
                    emptyText: Uni.I18n.translate('comPortPool.formFieldEmptyText.selectProtocolDetection', 'MDC', 'Select protocol detection...'),
                    store: 'Mdc.store.DeviceDiscoveryProtocols',
                    queryMode: 'local',
                    editable: false,
                    displayField: 'name',
                    valueField: 'id',
                    required: true,
                    width: 600
                },
                {
                    xtype: 'fieldcontainer',
                    hidden: true,
                    itemId: 'protocolDetectionDetails',
                    fieldLabel: Uni.I18n.translate('comportPool.protocolDetectionDetails', 'MDC', 'Protocol detection details'),
                    margin: '0 0 0 50',
                    labelAlign: 'top',
                    layout: 'vbox'
                },
                {
                    xtype: 'property-form',
                    itemId: 'property-form',
                    defaults: {
                        width: 335,
                        labelWidth: 250,
                        resetButtonHidden: true
                    },
                    width: 1000
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('comportpool.taskExecutionTimeout', 'MDC', 'Task Execution Timeout'),
                    itemId: 'taskExecutionTimeout',
                    required: true,
                    layout: 'hbox',
                    width: 600,
                    items: [
                        {
                            xtype: 'numberfield',
                            required: true,
                            allowBlank: false,
                            name: 'taskExecutionTimeout[count]',
                            itemId: 'taskExecutionTimeoutCount',
                            margin: '0 8 0 0',
                            width: 151,
                            minValue: 0,
                            stripCharsRe: /\D/,
                            value: 0
                        },
                        {
                            xtype: 'combobox',
                            required: true,
                            allowBlank: false,
                            editable: false,
                            name: 'taskExecutionTimeout[timeUnit]',
                            margin: '0 1 0 0',
                            flex: 1,
                            valueField: 'timeUnit',
                            displayField: 'localizedValue',
                            itemId: 'taskExecutionTimeoutUnit',
                            store: 'TimeUnitsWithoutMilliseconds'
                        }
                    ]
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
                            href: '#/administration/comportpools/'
                        }
                    ]
                }
            ]
        }
    ];
        me.callParent(arguments);
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

    numberFieldValidation: function (field) {
        var value = field.getValue();

        if (Ext.isEmpty(value) || value < field.minValue || value > field.maxValue) {
            field.setValue(field.minValue);
        }
    },

    changeMaxPriorityConnections: function() {
        var me =this,
            pctHighPrioTasks = me.down('#percentageOfPriorityTasks').getValue() ;
        if(this.edit && pctHighPrioTasks >= 0 && pctHighPrioTasks <= 100) {
            me.down('#maxPriorityConnections').show();
            Ext.Ajax.request({
                url: '/api/mdc/comportpools/' + me.down("[name=id]").getValue() + '/maxPriorityConnections',
                method: 'GET',
                params: {
                    pctHighPrioTasks: pctHighPrioTasks
                },
                success: function (operation) {
                    me.down('#maxPriorityConnections').setValue(operation.responseText);
                }
            })
        }
    },
});