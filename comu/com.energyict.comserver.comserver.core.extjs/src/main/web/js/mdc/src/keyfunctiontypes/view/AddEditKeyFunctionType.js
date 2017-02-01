/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.keyfunctiontypes.view.AddEditKeyFunctionType', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.key-function-type-add-form',
    requires: [
        'Uni.util.FormErrorMessage',
    ],
    isEdit: false,

    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'form',
            defaults: {
                labelWidth: 200,
                width: 500
            },
            ui: 'large',
            title: Uni.I18n.translate('general.addKeyFunctionType', 'MDC', 'Add key function type'),

            items: [
                {
                    xtype: 'uni-form-error-message',
                    itemId: 'key-function-type-error-message',
                    hidden: true
                },
                {
                    xtype: 'textfield',
                    name: 'name',
                    itemId: 'key-function-type-name-textfield',
                    fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                    required: true
                },
                {
                    xtype: 'textareafield',
                    name: 'description',
                    itemId: 'key-function-type-description-textfield',
                    fieldLabel: Uni.I18n.translate('general.description', 'MDC', 'Description'),
                    height: 80
                },
                {
                    xtype: 'combo-returned-record-data',
                    name: 'keyType',
                    itemId: 'key-function-type-key-type-combobox',
                    fieldLabel: Uni.I18n.translate('general.keyType', 'MDC', 'Key type'),
                    required: true,
                    //store: 'Mdc.keyfunctiontypes.store.KeyTypes',
                    queryMode: 'local',
                    displayField: 'name',
                    valueField: 'id',
                    forceSelection: true,
                    valueIsRecordData: true
                },
                {
                    xtype: 'fieldcontainer',
                    itemId: 'key-function-type-validity-period',
                    fieldLabel: Uni.I18n.translate('general.validityPeriod', 'MDC', 'Validity period'),
                    required: true,
                    width: 500,
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
                                    itemId: 'num-key-function-type-validity-period',
                                    name: 'validityPeriod[count]',
                                    maskRe: /[0-9]+/,
                                    width: 70,
                                    margin: '0 10 0 0',
                                    minValue: 1,
                                    value: 1
                                },
                                {
                                    xtype: 'combobox',
                                    name: 'validityPeriod[timeUnit]',
                                    itemId: 'cbo-key-function-type-validity-period-delay',
                                    store: 'Mdc.store.TimeUnits',
                                    queryMode: 'local',
                                    editable: false,
                                    displayField: 'localizedValue',
                                    valueField: 'timeUnit',
                                    flex: 1
                                }
                            ]
                        }
                    ]
                },
                {
                    xtype: 'fieldcontainer',
                    itemId: 'key-function-type-form-buttons',
                    fieldLabel: ' ',
                    layout: 'hbox',
                    margin: '20 0 0 0',
                    items: [
                        {
                            xtype: 'button',
                            itemId: 'key-function-type-add-button',
                            text: me.isEdit
                                ? Uni.I18n.translate('general.save', 'MDC', 'Save')
                                : Uni.I18n.translate('general.add', 'MDC', 'Add'),
                            ui: 'action',
                            action: me.isEdit
                                ? 'saveKeyFunctionType'
                                : 'addKeyFunctionType'
                        },
                        {
                            xtype: 'button',
                            itemId: 'key-function-type-cancel-add-button',
                            text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                            ui: 'link',
                            action: 'cancelAddEditKeyFunctionType'
                        }
                    ]
                }
            ]

        };
        me.callParent(arguments);
    },

    loadRecord: function (record) {
        var me = this;

        Ext.suspendLayouts();
        me.down('#key-function-type-key-type-combobox').setDisabled(true);
        me.callParent(arguments);
        Ext.resumeLayouts(true);
    }
});