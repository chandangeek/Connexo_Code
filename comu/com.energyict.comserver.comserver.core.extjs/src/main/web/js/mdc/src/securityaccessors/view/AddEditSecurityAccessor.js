/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.view.AddEditSecurityAccessor', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.security-accessor-add-form',
    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.form.field.ComboReturnedRecordData',
        'Mdc.store.TimeUnitsYearsSeconds',
        'Mdc.securityaccessors.store.KeyTypes',
        'Mdc.securityaccessors.store.KeyEncryptionMethods'
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
                    required: true,
                    allowBlank: false
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
                    disabled: me.isEdit,
                    items: [
                        {
                            boxLabel: Uni.I18n.translate('general.key', 'MDC', 'Key'),
                            itemId: 'mdc-security-accessor-key',
                            name: 'key',
                            inputValue: true,
                            listeners: {
                                change: me.onAccessorTypeChange
                            }
                        },
                        {
                            boxLabel: Uni.I18n.translate('general.certificate', 'MDC', 'Certificate'),
                            itemId: 'mdc-security-accessor-certificate',
                            name: 'key',
                            inputValue: false,
                            listeners: {
                                change: me.onAccessorTypeChange
                            }
                        }
                    ]
                },
                {
                    xtype: 'combo-returned-record-data',
                    name: 'keyType',
                    itemId: 'mdc-security-accessor-key-type-combobox',
                    fieldLabel: Uni.I18n.translate('general.keyType', 'MDC', 'Key type'),
                    required: true,
                    allowBlank: false,
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
                    fieldLabel: Uni.I18n.translate('general.trustStore', 'MDC', 'Trust store'),
                    hidden: true,
                    itemId: 'mdc-security-accessor-trust-store-container',
                    msgTarget: 'under',
                    required: true,
                    layout: 'hbox',
                    items: [
                        {
                            xtype: 'combo',
                            itemId: 'mdc-security-accessor-trust-store-combobox',
                            store: 'Mdc.securityaccessors.store.TrustStores',
                            name: 'trustStoreId',
                            width: 285,
                            hidden: true,
                            required: true,
                            allowBlank: false,
                            displayField: 'name',
                            valueField: 'id',
                            forceSelection: true,
                            disabled: me.isEdit,
                            emptyText: Uni.I18n.translate('securityaccessors.selectTrustStore','MDC', 'Select a trust store...')
                        },
                        {
                            xtype: 'component',
                            html: Uni.I18n.translate('general.noTrustStores', 'MDC', 'No trust stores defined yet'),
                            itemId: 'mdc-security-accessor-no-trust-store-msg',
                            hidden: true,
                            width: 285,
                            style: {
                                'color': '#FF0000',
                                'margin': '6px 10px 6px 0px'
                            }
                        }
                    ]
                },
                {
                    xtype: 'combo',
                    fieldLabel: Uni.I18n.translate('general.storageMethod', 'MDC', 'Storage method'),
                    itemId: 'mdc-security-accessor-storage-method-combobox',
                    name: 'storageMethod',
                    required: true,
                    allowBlank: false,
                    displayField: 'name',
                    valueField: 'name',
                    forceSelection: true,
                    disabled: me.isEdit,
                    emptyText: Uni.I18n.translate('securityaccessors.selectStorageMethod','MDC', 'Select a storage method...')
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
                            itemId: 'mdc-security-accessor-cancel-link',
                            text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                            ui: 'link',
                            action: 'cancelAddEditSecurityAccessor'
                        }
                    ]
                }
            ]

        };
        me.callParent(arguments);
        if (!me.isEdit) {
            me.on('boxready', function () {
                me.down('#mdc-security-accessor-key').setValue(true);
            }, me, {single: true});
        }
    },

    onAccessorTypeChange: function(radioBtn, newValue) {
        var me = this,
            errorMsgPnl = me.up('form').down('#mdc-security-accessor-error-message'),
            key = radioBtn.itemId === 'mdc-security-accessor-key',
            typeCombo = me.up('form').down('#mdc-security-accessor-key-type-combobox'),
            storageMethodCombo = me.up('form').down('#mdc-security-accessor-storage-method-combobox'),
            trustStoreCombo = me.up('form').down('#mdc-security-accessor-trust-store-combobox'),
            trustStoreContainer = me.up('form').down('#mdc-security-accessor-trust-store-container'),
            noTrustStoreMsg = me.up('form').down('#mdc-security-accessor-no-trust-store-msg'),
            accessorTypeIsKey = ( (radioBtn.itemId === 'mdc-security-accessor-key' && newValue) ||
                                  (radioBtn.itemId === 'mdc-security-accessor-certificate' && !newValue) );

        storageMethodCombo.setValue(null);
        if (accessorTypeIsKey) {
            storageMethodCombo.show();
            storageMethodCombo.setDisabled(true);
        } else {
            storageMethodCombo.hide();
        }

        if (newValue) {
            errorMsgPnl.hide();
            trustStoreContainer.setVisible(!key);
            me.up('form').getForm().clearInvalid();
            trustStoreCombo.allowBlank = key;
            typeCombo.setFieldLabel(key
                ? Uni.I18n.translate('general.keyType', 'MDC', 'Key type')
                : Uni.I18n.translate('general.certificateType', 'MDC', 'Certificate type')
            );
            typeCombo.emptyText = key
                ? Uni.I18n.translate('securityaccessors.selectKeyType','MDC', 'Select a key type...')
                : Uni.I18n.translate('securityaccessors.selectCertificateType','MDC', 'Select a certificate type...');
            typeCombo.setValue(null);
            typeCombo.getStore().clearFilter(true);
            typeCombo.getStore().filter([
                {
                    filterFn: function(item) {
                        return key ? item.get("isKey") : !item.get("isKey");
                    }
                }
            ]);
            if (typeCombo.getStore().getCount()===1) {
                typeCombo.setValue(typeCombo.getStore().getAt(0).get('id'));
            }
            typeCombo.clearInvalid();
            noTrustStoreMsg.hide();
            trustStoreCombo.show();
            if (trustStoreCombo.getStore().getCount()===0) {
                noTrustStoreMsg.show();
                trustStoreCombo.hide();
            } else if (trustStoreCombo.getStore().getCount()===1) {
                trustStoreCombo.setValue(trustStoreCombo.getStore().getAt(0).get('id'));
            }
        }
    }
});