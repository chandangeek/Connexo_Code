/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.AddCSRForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.csr-add-form',

    requires: [
        'Uni.util.FormErrorMessage'
    ],
    margin: '15 0 0 0',

    cancelLink: undefined,

    defaults: {
        labelWidth: 260,
        width: 600,
        msgTarget: 'under'
    },

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'uni-form-error-message',
                itemId: 'pkj-csr-add-form-errors',
                margin: '0 0 10 0',
                hidden: true
            },
            {
                xtype: 'textfield',
                fieldLabel: Uni.I18n.translate('general.alias', 'PKJ', 'Alias'),
                required: true,
                name: 'alias',
                itemId: 'pkj-csr-add-form-alias',
                allowBlank: false
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('general.keyAttribute', 'PKJ', 'Key attribute'),
                labelAlign: 'left'
            },
            {
                xtype: 'combo',
                fieldLabel: Uni.I18n.translate('general.keyEncryptionMethod', 'PKJ', 'Key encryption method'),
                store: 'Pkj.store.KeyEncryptionMethods',
                displayField: 'displayValue',
                valueField: 'name',
                itemId: 'pkj-csr-add-form-key-encryption-method-combo',
                name: 'keyEncryptionMethod',
                required: true,
                allowBlank: false
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('general.csrAttribute', 'PKJ', 'CSR attributes'),
                labelAlign: 'left'
            },
            {
                xtype: 'combo',
                fieldLabel: Uni.I18n.translate('general.certificateType', 'PKJ', 'Certificate type'),
                store: 'Pkj.store.CertificateTypes',
                displayField: 'displayValue',
                valueField: 'name',
                itemId: 'pkj-csr-add-form-certificate-type-combo',
                name: 'keyTypeId',
                required: true,
                allowBlank: false
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.subject', 'PKJ', 'Subject'),
                itemId: 'pkj-csr-add-form-subject',
                renderer: function(value) {
                    if (Ext.isEmpty(value)) {
                        return Uni.I18n.translate('general.csrAttribute.subject.emptyText', 'PKJ', 'Fill in attributes below to preview subject');
                    }
                    return value;
                }
            },
            {
                xtype: 'textfield',
                fieldLabel: Uni.I18n.translate('general.csrAttribute.commonName', 'PKJ', 'Common name (CN)'),
                required: true,
                name: 'CN',
                itemId: 'pkj-csr-add-form-common-name',
                allowBlank: false,
                listeners: {
                    change: {
                        fn: me.onFieldChange,
                        scope: me
                    }
                }
            },
            {
                xtype: 'textfield',
                fieldLabel: Uni.I18n.translate('general.csrAttribute.organisationalUnit', 'PKJ', 'Organisational unit (OU)'),
                name: 'OU',
                itemId: 'pkj-csr-add-form-organisational-unit',
                listeners: {
                    change: {
                        fn: me.onFieldChange,
                        scope: me
                    }
                }
            },
            {
                xtype: 'textfield',
                fieldLabel: Uni.I18n.translate('general.csrAttribute.organisation', 'PKJ', 'Organisation (O)'),
                name: 'O',
                itemId: 'pkj-csr-add-form-organisation',
                listeners: {
                    change: {
                        fn: me.onFieldChange,
                        scope: me
                    }
                }
            },
            {
                xtype: 'textfield',
                fieldLabel: Uni.I18n.translate('general.csrAttribute.locality', 'PKJ', 'Locality (L)'),
                name: 'L',
                itemId: 'pkj-csr-add-form-locality',
                listeners: {
                    change: {
                        fn: me.onFieldChange,
                        scope: me
                    }
                }
            },
            {
                xtype: 'textfield',
                fieldLabel: Uni.I18n.translate('general.csrAttribute.stateOrProvince', 'PKJ', 'State or province (S)'),
                name: 'S',
                itemId: 'pkj-csr-add-form-state',
                listeners: {
                    change: {
                        fn: me.onFieldChange,
                        scope: me
                    }
                }
            },
            {
                xtype: 'textfield',
                fieldLabel: Uni.I18n.translate('general.csrAttribute.country', 'PKJ', 'Country (C)'),
                name: 'C',
                itemId: 'pkj-csr-add-form-country',
                listeners: {
                    change: {
                        fn: me.onFieldChange,
                        scope: me
                    }
                }
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: '&nbsp;'
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: '&nbsp;',
                layout: {
                    type: 'hbox'
                },
                items: [
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.add', 'PKJ', 'Add'),
                        ui: 'action',
                        itemId: 'pkj-csr-add-form-add-btn'
                    },
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.cancel', 'PKJ', 'Cancel'),
                        ui: 'link',
                        itemId: 'pkj-csr-add-form-cancel-link',
                        href: me.cancelLink
                    }
                ]
            }
        ];
        me.callParent(arguments);
    },

    onFieldChange: function(field) {
        this.down('#pkj-csr-add-form-subject').setValue(this.constructSubject());
    },

    constructSubject: function() {
        var me = this,
            cnField = me.down('#pkj-csr-add-form-common-name'),
            ouField = me.down('#pkj-csr-add-form-organisational-unit'),
            oField = me.down('#pkj-csr-add-form-organisation'),
            lField = me.down('#pkj-csr-add-form-locality'),
            sField = me.down('#pkj-csr-add-form-state'),
            cField = me.down('#pkj-csr-add-form-country'),
            result ='';

        if (!Ext.isEmpty(cnField.getValue())) {
            result += 'CN=' + cnField.getValue();
        }
        if (!Ext.isEmpty(ouField.getValue())) {
            if (result.length>0) { result += ', '; }
            result += 'OU=' + ouField.getValue();
        }
        if (!Ext.isEmpty(oField.getValue())) {
            if (result.length>0) { result += ', '; }
            result += 'O=' + oField.getValue();
        }
        if (!Ext.isEmpty(lField.getValue())) {
            if (result.length>0) { result += ', '; }
            result += 'L=' + lField.getValue();
        }
        if (!Ext.isEmpty(sField.getValue())) {
            if (result.length>0) { result += ', '; }
            result += 'S=' + sField.getValue();
        }
        if (!Ext.isEmpty(cField.getValue())) {
            if (result.length>0) { result += ', '; }
            result += 'C=' + cField.getValue();
        }
        return result;
    }
});