/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.view.PreviewForm', {
    extend: 'Ext.panel.Panel',
    frame: false,
    alias: 'widget.devicetype-security-accessors-preview-form',
    layout: 'fit',

    requires: [
        'Uni.form.field.ExecutionLevelDisplay',
        'Mdc.securityaccessors.store.TrustStores'
    ],

    items: {
        xtype: 'form',
        layout: 'column',
        defaults: {
            xtype: 'container',
            layout: 'form',
            columnWidth: 0.5
        },
        items: []
    },

    doLoadRecord: function(record) {
        var me = this,
            leftItems = {
                defaults: {
                    xtype: 'displayfield'
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                        name: 'name'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.description', 'MDC', 'Description'),
                        name: 'description'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.accessorType', 'MDC', 'Accessor type'),
                        name: 'isKey',
                        renderer: function (value) {
                            return value
                                ? Uni.I18n.translate('general.key', 'MDC', 'Key')
                                : Uni.I18n.translate('general.certificate', 'MDC', 'Certificate');
                        }
                    },
                    {
                        fieldLabel: record.get('isKey')
                            ? Uni.I18n.translate('general.keyType', 'MDC', 'Key type')
                            : Uni.I18n.translate('general.certificateType', 'MDC', 'Certificate type'),
                        name: 'keyType',
                        renderer: function (value) {
                            return Ext.isEmpty(value) || Ext.isEmpty(value.name) ? '-' : value.name;
                        }
                    }
                ]
            },
            rightItems,
            form = me.down('form');

        form.removeAll();
        if (record.get('isKey')) {
            rightItems = {
                defaults: {
                    xtype: 'displayfield'
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('general.storageMethod', 'MDC', 'Storage method'),
                        name: 'storageMethod',
                        renderer: function (val) {
                            return Ext.isEmpty(val) ? '-' : val;
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.validityPeriod', 'MDC', 'Validity period'),
                        name: 'validityPeriod',
                        renderer: function (val) {
                            return Ext.isEmpty(val) ? '-' : val.count + ' ' + val.localizedTimeUnit;
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('securityaccessors.viewPrivileges', 'MDC', 'View privileges'),
                        xtype: 'execution-level-displayfield',
                        name: 'viewLevelsInfo'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('securityaccessors.editPrivileges', 'MDC', 'Edit privileges'),
                        xtype: 'execution-level-displayfield',
                        name: 'editLevelsInfo'
                    }
                ]
            }
        } else {
            rightItems = {
                defaults: {
                    xtype: 'displayfield'
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('general.trustStore', 'MDC', 'Trust store'),
                        name: 'trustStoreId',
                        renderer: function (val) {
                            if (Ext.isEmpty(val)) {
                                return '-';
                            }
                            var trustStoresStore = Ext.getStore('Mdc.securityaccessors.store.TrustStores'),
                                storeIndex = trustStoresStore.findExact('id', val);
                            return trustStoresStore.getAt(storeIndex).get('name');
                        }
                    }
                ]
            }
        }
        form.add([leftItems, rightItems]);
        form.loadRecord(record);
    }

});