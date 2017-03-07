/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.view.AddEditSecurityAccessor', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.security-accessor-add-form',
    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.form.field.ComboReturnedRecordData',
        'Mdc.store.TimeUnitsYearsSeconds'
    ],
    isEdit: false,
    title: null,

    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'form',
            defaults: {
                labelWidth: 200,
                width: 500
            },
            ui: 'large',
            title: me.isEdit ? me.title : Uni.I18n.translate('general.addSecurityAccessor', 'MDC', 'Add security accessor'),

            items: [
                {
                    xtype: 'uni-form-error-message',
                    itemId: 'mdc-security-accessor-error-message',
                    hidden: true
                },
                {
                    xtype: 'textfield',
                    name: 'name',
                    itemId: 'mdc-security-accessor-name-textfield',
                    fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                    required: true
                },
                {
                    xtype: 'textareafield',
                    name: 'description',
                    itemId: 'mdc-security-accessor-description-textfield',
                    fieldLabel: Uni.I18n.translate('general.description', 'MDC', 'Description'),
                    height: 80
                },
                {
                    xtype: 'radiogroup',
                    fieldLabel: Uni.I18n.translate('general.accessorType', 'MDC', 'Accessor type'),
                    columns: 1,
                    required: true,
                    vertical: true,
                    items: [
                        {
                            boxLabel: Uni.I18n.translate('general.key', 'MDC', 'Key'),
                            itemId: 'mdc-security-accessor-key',
                            name: 'key',
                            inputValue: true,
                            checked: true
                        },
                        {
                            boxLabel: Uni.I18n.translate('general.certificate', 'MDC', 'Certificate'),
                            itemId: 'mdc-security-accessor-certificate',
                            name: 'key',
                            inputValue: false
                        }
                    ]
                },
                {
                    xtype: 'combo-returned-record-data',
                    name: 'keyType',
                    itemId: 'mdc-security-accessor-key-type-combobox',
                    fieldLabel: Uni.I18n.translate('general.keyType', 'MDC', 'Key type'),
                    required: true,
                    store: 'Mdc.securityaccessors.store.KeyTypes',
                    queryMode: 'local',
                    displayField: 'name',
                    valueField: 'id',
                    forceSelection: true,
                    valueIsRecordData: true,
                    disabled: me.isEdit,
                    emptyText: Uni.I18n.translate('securityaccessors.selectKeyType','MDC', 'Select a key type...')
                },
                {
                    xtype: 'fieldcontainer',
                    itemId: 'mdc-security-accessor-validity-period',
                    fieldLabel: Uni.I18n.translate('general.validityPeriod', 'MDC', 'Validity period'),
                    hidden: true,
                    required: true,
                    width: 500,
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
                                    itemId: 'num-security-accessor-validity-period',
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
                                    itemId: 'cbo-security-accessor-validity-period-delay',
                                    store: 'Mdc.store.TimeUnitsYearsSeconds',
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
                    itemId: 'mdc-security-accessor-form-buttons',
                    fieldLabel: ' ',
                    layout: 'hbox',
                    margin: '20 0 0 0',
                    items: [
                        {
                            xtype: 'button',
                            itemId: 'mdc-security-accessor-add-button',
                            text: me.isEdit
                                ? Uni.I18n.translate('general.save', 'MDC', 'Save')
                                : Uni.I18n.translate('general.add', 'MDC', 'Add'),
                            ui: 'action',
                            action: me.isEdit ? 'saveSecurityAccessor' : 'addSecurityAccessor'
                        },
                        {
                            xtype: 'button',
                            itemId: 'mdc-security-accessor-cancel-add-button',
                            text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                            ui: 'link',
                            action: 'cancelAddEditSecurityAccessor'
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
        me.down('#mdc-security-accessor-key-type-combobox').setDisabled(true);
        me.callParent(arguments);
        Ext.resumeLayouts(true);
    }
});