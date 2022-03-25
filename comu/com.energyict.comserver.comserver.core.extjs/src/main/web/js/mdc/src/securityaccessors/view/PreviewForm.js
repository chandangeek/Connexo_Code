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

    doLoadRecord: function (record, defaultKeyValue, deviceTypeId) {
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
                        fieldLabel: Uni.I18n.translate('general.purpose', 'MDC', 'Purpose'),
                        name: 'purpose',
                        renderer: function (value) {
                            return value ? value.name : '-';
                        }
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
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.keyPurpose', 'MDC', 'Key purpose'),
                        name: 'keyPurpose',
                        hidden: record.get('isKey') ? false : true,
                        renderer: function (value) {
                            return Ext.isEmpty(value) || Ext.isEmpty(value.name) ? '-' : value.name;
                        }
                    },
                    {
                        xtype: 'displayfield',
                        fieldLabel: Uni.I18n.translate('keyRenewal.command', 'MDC', 'Key renewal command'),
                        itemId: 'previewPropertiesCommandName',
                    },
                    {
                        xtype: 'keyRenewalOverviewProperties',
                        commandTypePrefix: '',
                        itemId: 'keyRenewalOverviewProperties'
                    },
                    {
                        xtype: 'displayfield',
                        fieldLabel: Uni.I18n.translate('serviceKeyRenewal.command', 'MDC', 'Service key renewal command'),
                        itemId: 'previewPropertiesServiceKeyCommandName',
                    },
                    {
                        xtype: 'keyRenewalOverviewProperties',
                        commandTypePrefix: 'serviceKey-',
                        itemId: 'serviceKeyRenewalOverviewProperties'
                    },
                    {
                        xtype: 'displayfield',
                        fieldLabel: Uni.I18n.translate('key.wrapper.label', 'MDC', 'Wrapped by'),
                        itemId: 'previewWrapperName',
                    }
                ]
            },
            rightItems,
            form = me.down('form');

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
                        fieldLabel: Uni.I18n.translate('general.label', 'MDC', 'Label'),
                        name: 'label',
                        renderer: function (val) {
                            return Ext.isEmpty(val) ? '-' : val;
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.validityPeriod', 'MDC', 'Validity period'),
                        name: 'duration',
                        renderer: function (val) {
                            return Ext.isEmpty(val) ? '-' : Uni.I18n.translatePlural('general.timeUnit.years', val.count, 'MDC', '{0} years', '{0} year', '{0} years');
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
                    },
                    {
                        fieldLabel: Uni.I18n.translate('securityaccessors.defaultServiceKey', 'MDC', 'Default service key'),
                        name: 'defaultServiceKey',
                        hidden: !(record.get('keyType').name == 'HSM Key' && deviceTypeId),
                        renderer: function () {
                            var val = defaultKeyValue;
                            return Ext.isEmpty(val) ? '-' : val;
                        }
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
                            return storeIndex > -1 ? trustStoresStore.getAt(storeIndex).get('name') : '-';
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.manageCentrally', 'MDC', 'Manage centrally'),
                        name: 'manageCentrally',
                        renderer: function (val) {
                            if (Ext.isEmpty(val)) {
                                return Uni.I18n.translate('general.no', 'MDC', 'No');
                            } else {
                                return Uni.I18n.translate('general.yes', 'MDC', 'Yes');
                            }

                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.activeCertificate', 'MDC', 'Active certificate'),
                        name: 'activeCertificate',
                        hidden: !record.get('manageCentrally'),
                        renderer: function (val) {
                            if (Ext.isEmpty(val)) {
                                return '-';
                            } else {
                                return val;
                            }

                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.passiveCertificate', 'MDC', 'Passive certificate'),
                        name: 'passiveCertificate',
                        hidden: !record.get('manageCentrally'),
                        renderer: function (val) {
                            if (Ext.isEmpty(val)) {
                                return '-';
                            } else {
                                return val;
                            }

                        }
                    }
                ]
            }
        }

        Ext.suspendLayouts();
        form.removeAll();
        form.add([leftItems, rightItems]);
        form.loadRecord(record);
        Ext.resumeLayouts(true);
    }

});