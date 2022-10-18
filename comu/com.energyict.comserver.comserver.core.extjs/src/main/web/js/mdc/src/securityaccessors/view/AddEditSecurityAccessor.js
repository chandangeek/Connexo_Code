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
        'Mdc.securityaccessors.store.TrustStores',
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
                    allowBlank: false,
                    vtype: 'checkForBlacklistCharacters',
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
                    fieldLabel: Uni.I18n.translate('general.purpose', 'MDC', 'Purpose'),
                    itemId: 'mdc-security-accessor-purpose-radio',
                    name: 'purpose',
                    columns: 1,
                    required: true,
                    vertical: true,
                    editable : false,
                    disabled: me.isEdit,
                    listeners: {
                        change: me.manageCentrallyChange
                    },
                    items: [
                        {
                            boxLabel: Uni.I18n.translate('general.deviceOperations', 'MDC', 'Device operations'),
                            name: 'purpose',
                            itemId: 'mdc-security-accessor-purpose-device-operations',
                            inputValue: {id: "DEVICE_OPERATIONS"},
                            checked: true
                        },
                        {
                            boxLabel: Uni.I18n.translate('general.fileOperations', 'MDC', 'File operations'),
                            name: 'purpose',
                            itemId: 'mdc-security-accessor-purpose-file-operations',
                            inputValue: {id: "FILE_OPERATIONS"}
                        }
                    ]
                },
                {
                    xtype: 'radiogroup',
                    fieldLabel: Uni.I18n.translate('general.accessorType', 'MDC', 'Accessor type'),
                    itemId: 'mdc-security-accessor-key-radio',
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
                    editable: false,
                    valueIsRecordData: true,
                    disabled: me.isEdit,
                    emptyText: Uni.I18n.translate('securityaccessors.selectKeyType','MDC', 'Select a key type...')
                },
                {
                    xtype: 'combo-returned-record-data',
                    name: 'keyPurpose',
                    itemId: 'mdc-security-accessor-key-purpose-combobox',
                    fieldLabel: Uni.I18n.translate('general.keyPurpose', 'MDC', 'Key purpose'),
                    store: 'Mdc.securityaccessors.store.KeyPurposes',
                    queryMode: 'local',
                    displayField: 'name',
                    valueField: 'key',
                    required: true,
                    allowBlank: false,
                    editable: false,
                    valueIsRecordData: true,
                    forceSelection: true,
                    emptyText: Uni.I18n.translate('securityaccessors.selectKeyPurpose','MDC', 'Select a key purpose...')
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('general.trustStore', 'MDC', 'Trust store'),
                    hidden: true,
                    itemId: 'mdc-security-accessor-trust-store-container',
                    msgTarget: 'under',
                    required: true,
                    disabled: me.isEdit,
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
                            editable : false,
                            allowBlank: false,
                            displayField: 'name',
                            valueField: 'id',
                            forceSelection: true,
                            editable : false,
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
                                    name: 'duration[count]',
                                    maskRe: /[0-9]+/,
                                    width: 70,
                                    margin: '0 10 0 0',
                                    minValue: 1,
                                    value: 1
                                },
                                {
                                    xtype: 'combobox',
                                    name: 'duration[localizedTimeUnit]',
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
                    xtype: 'combo',
                    fieldLabel: Uni.I18n.translate('general.hsm.jss.keytype', 'MDC', 'Key subtype'),
                    itemId: 'mdc-security-accessor-jss-keytype-combobox',
                    name: 'hsmJssKeyType',
                    store: 'Mdc.securityaccessors.store.HsmJssKeyTypes',
                    hidden: true,
                    required: true,
                    displayField: 'name',
                    valueField: 'name',
                    forceSelection: true,
                    disabled: me.isEdit,
                    emptyText: Uni.I18n.translate('securityaccessors.selectHsmJssKeyType', 'MDC', 'Select a key type...')
                },
                {
                    xtype: 'combo',
                    fieldLabel: Uni.I18n.translate('general.importCapability', 'MDC', 'Import capability'),
                    itemId: 'mdc-security-accessor-import-capability-combobox',
                    name: 'importCapability',
                    store: 'Mdc.securityaccessors.store.HsmCapabilities',
                    hidden: true,
                    required: true,
                    displayField: 'name',
                    valueField: 'name',
                    forceSelection: true,
                    disabled: me.isEdit,
                    emptyText: Uni.I18n.translate('securityaccessors.selectImportCapability', 'MDC', 'Select an import capability...')
                },
                {
                    xtype: 'combo',
                    fieldLabel: Uni.I18n.translate('general.renewCapability', 'MDC', 'Renew capability'),
                    itemId: 'mdc-security-accessor-renew-capability-combobox',
                    name: 'renewCapability',
                    store: 'Mdc.securityaccessors.store.HsmCapabilities',
                    hidden: true,
                    required: true,
                    displayField: 'name',
                    valueField: 'name',
                    forceSelection: true,
                    disabled: me.isEdit,
                    emptyText: Uni.I18n.translate('securityaccessors.selectRenewCapability', 'MDC', 'Select a renew capability...')
                },
                {
                    xtype: 'combo',
                    fieldLabel: Uni.I18n.translate('general.labelEndPoint', 'MDC', 'Storage label'),
                    itemId: 'mdc-security-accessor-label-end-point-combobox',
                    name: 'label',
                    store: 'Mdc.securityaccessors.store.HSMLabelEndPoint',
                    hidden: true,
                    required: true,
                    displayField: 'name',
                    valueField: 'name',
                    forceSelection: true,
                    disabled: me.isEdit,
                    emptyText: Uni.I18n.translate('securityaccessors.selectLabelEndPoint', 'MDC', 'Select a storage label ...')
                },
                {
                    xtype: 'textfield',
                    name: 'keySize',
                    itemId: 'mdc-security-accessor-key-size',
                    fieldLabel: Uni.I18n.translate('general.keySizes', 'MDC', 'Size (bytes)'),
                    hidden: true,
                    required: true,
                    allowBlank: false,
                    vtype: 'checkForBlacklistCharacters',
                    emptyText: Uni.I18n.translate('securityaccessors.keySizes', 'MDC', 'Input size ...')
                },
                {
                    xtype: 'checkboxfield',
                    fieldLabel: Uni.I18n.translate('general.isReversible', 'MDC', 'Is reversible'),
                    itemId: 'mdc-security-accessor-isReversible-checkbox',
                    name: 'isReversible',
                    hidden: true,
                    checked: false,
                    disabled: me.isEdit
                },
                {
                    xtype: 'checkboxfield',
                    fieldLabel: Uni.I18n.translate('general.isWrapper', 'MDC', 'Is wrapper'),
                    itemId: 'mdc-security-accessor-isWrapper-checkbox',
                    name: 'isWrapper',
                    hidden: true,
                    checked: false,
                    disabled: me.isEdit
                },
                {
                    xtype: 'checkboxfield',
                    fieldLabel: Uni.I18n.translate('general.manageCentrally', 'MDC', 'Manage centrally'),
                    itemId: 'mdc-security-accessor-manage-centrally-checkbox',
                    name: 'multiElementEnabled',
                    checked: false,
                    disabled: true
                },
                {
                    xtype: 'fieldcontainer',
                    itemId: 'mdc-active-passive-certificates',
                    fieldLabel: ' ',
                    layout: 'vbox',
                    labelWidth: 0,
                    width: 500,
                    items: []
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

    manageCentrallyChange: function(target) {
        var form = target.up('form'),
            keyRadio = form.down('#mdc-security-accessor-key-radio'),
            manageCentrallyCheck = form.down('#mdc-security-accessor-manage-centrally-checkbox');

        if (target.getValue().purpose.id === 'FILE_OPERATIONS' && !keyRadio.getValue().key) {
            manageCentrallyCheck.setValue(true);
            manageCentrallyCheck.setDisabled(true);
        } else {
            manageCentrallyCheck.setValue(false);
            manageCentrallyCheck.setDisabled(false);
        }
    },

    onAccessorTypeChange: function(radioBtn, newValue) {
        var me = this,
            form = me.up('form'),
            errorMsgPnl = form.down('#mdc-security-accessor-error-message'),
            key = radioBtn.itemId === 'mdc-security-accessor-key',
            typeCombo = form.down('#mdc-security-accessor-key-type-combobox'),
            keyPurposeCombo = form.down('#mdc-security-accessor-key-purpose-combobox'),
            purposesRadio = form.down('#mdc-security-accessor-purpose-radio'),
            storageMethodCombo = form.down('#mdc-security-accessor-storage-method-combobox'),
            trustStoreCombo = form.down('#mdc-security-accessor-trust-store-combobox'),
            trustStoreContainer = form.down('#mdc-security-accessor-trust-store-container'),
            manageCentrallyCheckbox = form.down('#mdc-security-accessor-manage-centrally-checkbox'),
            noTrustStoreMsg = form.down('#mdc-security-accessor-no-trust-store-msg'),
            accessorTypeIsKey = ( (radioBtn.itemId === 'mdc-security-accessor-key' && newValue) ||
                (radioBtn.itemId === 'mdc-security-accessor-certificate' && !newValue) );

        storageMethodCombo.setValue(null);
        if (accessorTypeIsKey) {
            storageMethodCombo.show();
            storageMethodCombo.setDisabled(true);
        } else {
            storageMethodCombo.hide();
        }

        form.up('security-accessor-add-form').manageCentrallyChange(purposesRadio);

        if (newValue) {
            errorMsgPnl.hide();
            trustStoreContainer.setVisible(!key);
            manageCentrallyCheckbox.setVisible(!key);
            me.up('form').getForm().clearInvalid();
            var proxy = trustStoreCombo.getStore().getProxy();
            proxy.limitParam = undefined;
            proxy.startParam = undefined;
            proxy.pageParam = undefined;

            trustStoreCombo.allowBlank = key;
            keyPurposeCombo.allowBlank = !key;
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
            key ? keyPurposeCombo.show() : keyPurposeCombo.hide();
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
