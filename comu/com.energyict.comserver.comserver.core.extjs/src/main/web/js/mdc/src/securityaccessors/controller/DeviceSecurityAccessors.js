/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.controller.DeviceSecurityAccessors', {
    extend: 'Ext.app.Controller',

    stores: [
        'Mdc.securityaccessors.store.DeviceSecurityKeys',
        'Mdc.securityaccessors.store.DeviceSecurityCertificates',
        'Mdc.securityaccessors.store.CertificateAliases'
    ],

    models: [
        'Mdc.securityaccessors.model.DeviceSecurityKey',
        'Mdc.securityaccessors.model.DeviceSecurityCertificate',
        'Mdc.securityaccessors.model.CertificateAlias'
    ],

    views: [
        'Mdc.securityaccessors.view.DeviceSecurityAccessorsOverview',
        'Mdc.securityaccessors.view.EditDeviceKeyAttributes',
        'Mdc.securityaccessors.view.EditDeviceCertificateAttributes',
        'Mdc.securityaccessors.view.PrivilegesHelper'
    ],

    refs: [
        {
            ref: 'keyPreview',
            selector: '#mdc-device-accessors-key-preview'
        },
        {
            ref: 'certificatePreviewForm',
            selector: '#mdc-device-accessors-certificate-preview device-security-accessor-preview-form form'
        },
        {
            ref: 'certificatePreview',
            selector: '#mdc-device-accessors-certificate-preview'
        },
        {
            ref: 'tabPanel',
            selector: '#mdc-device-security-accessors-tab-panel'
        },
        {
            ref: 'activeKeyAttributesContainer',
            selector: '#mdc-device-accessors-key-preview #mdc-device-security-accessor-preview-active-attributes-container'
        },
        {
            ref: 'passiveKeyAttributesContainer',
            selector: '#mdc-device-accessors-key-preview #mdc-device-security-accessor-preview-passive-attributes-container'
        },
        {
            ref: 'activeCertificateAttributesContainer',
            selector: '#mdc-device-accessors-certificate-preview #mdc-device-security-accessor-preview-active-attributes-container'
        },
        {
            ref: 'passiveCertificateAttributesContainer',
            selector: '#mdc-device-accessors-certificate-preview #mdc-device-security-accessor-preview-passive-attributes-container'
        },
        {
            ref: 'editActiveKeyAttributesContainer',
            selector: '#mdc-device-key-attributes-edit-active-attributes-container'
        },
        {
            ref: 'editPassiveKeyAttributesContainer',
            selector: '#mdc-device-key-attributes-edit-passive-attributes-container'
        },
        {
            ref: 'editActiveCertificateAttributesContainer',
            selector: '#mdc-device-certificate-attributes-edit-active-attributes-container'
        },
        {
            ref: 'editPassiveCertificateAttributesContainer',
            selector: '#mdc-device-certificate-attributes-edit-passive-attributes-container'
        },
        {
            ref: 'editDeviceKeyPanel',
            selector: 'device-key-attributes-edit'
        },
        {
            ref: 'editDeviceCertificatePanel',
            selector: 'device-certificate-attributes-edit'
        },
        {
            ref: 'keysGrid',
            selector: '#mdc-device-accessors-keys-grid'
        },
        {
            ref: 'certificatesGrid',
            selector: '#mdc-device-accessors-certificates-grid'
        }
    ],

    deviceId: undefined,
    deviceKeyRecord: undefined,
    deviceCertificateRecord: undefined,

    init: function () {
        var me = this;

        me.control({
            'device-security-accessors-overview #mdc-device-accessors-keys-grid': {
                select: me.onKeyRecordSelected
            },
            'device-security-accessors-overview #mdc-device-accessors-certificates-grid': {
                select: me.onCertificateRecordSelected
            },
            'device-security-accessors-action-menu': {
                click: me.onMenuAction
            },
            '#mdc-device-security-accessors-tab-panel': {
                tabchange: me.onTabChange
            },
            '#mdc-device-key-attributes-edit-cancel-link': {
                click: me.navigateToKeysOverview
            },
            '#mdc-device-certificate-attributes-edit-cancel-link': {
                click: me.navigateToCertificatesOverview
            },
            '#mdc-device-key-attributes-edit-save-button': {
                click: me.saveKeyAttributes
            },
            '#mdc-device-certificate-attributes-edit-save-button': {
                click: me.saveCertificateAttributes
            }
        });
    },

    navigateToKeysOverview: function () {
        location.href = '#/devices/' + encodeURIComponent(this.deviceId) + '/keys/';
    },

    navigateToCertificatesOverview: function () {
        location.href = '#/devices/' + encodeURIComponent(this.deviceId) + '/certificates/';
    },

    onTabChange: function(tabPanel, newActiveItem) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            url = router.getRoute().buildUrl(router.arguments, router.queryParams);

        if (newActiveItem.itemId === 'mdc-device-accessors-keys-tab') {
            url = url.replace('/certificates', '/keys');
            me.onKeyRecordSelected(me.getKeysGrid(), me.getKeysGrid() ? me.getKeysGrid().getSelectionModel().getSelection()[0] : null);
        } else if (newActiveItem.itemId === 'mdc-device-accessors-certificates-tab') {
            url = url.replace('/keys', '/certificates');
            me.onCertificateRecordSelected(me.getCertificatesGrid(), me.getCertificatesGrid() ? me.getCertificatesGrid().getSelectionModel().getSelection()[0] : null);
        }
        Uni.util.History.setParsePath(false);
        Uni.util.History.suspendEventsForNextCall();
        window.location.replace(url);
    },

    showDeviceKeys: function(deviceId) {
        this.deviceId = deviceId;
        this.showDeviceSecurityAccessors(deviceId, 0);
    },

    showDeviceCertificates: function(deviceId) {
        this.deviceId = deviceId;
        this.showDeviceSecurityAccessors(deviceId, 1);
    },

    showDeviceSecurityAccessors: function(deviceId, activeTab) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0];

        me.deviceId = deviceId;

        viewport.setLoading();
        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceId, {
            success: function (device) {
                var overViewWidget = Ext.create('Mdc.securityaccessors.view.DeviceSecurityAccessorsOverview', {
                    device: device,
                    activeTab: activeTab
                });
                me.getApplication().fireEvent('changecontentevent', overViewWidget);
                me.getApplication().fireEvent('loadDevice', device);
                viewport.setLoading(false);
            }
        });
    },

    onKeyRecordSelected: function (grid, record) {
        if (Ext.isEmpty(record)) return;
        var me = this,
            actionsMenu = me.getKeyPreview().down('device-security-accessors-action-menu'),
            hasViewRights = Mdc.securityaccessors.view.PrivilegesHelper.hasPrivileges(record.get('viewLevels')),
            hasEditRights = Mdc.securityaccessors.view.PrivilegesHelper.hasPrivileges(record.get('editLevels')),
            tempPropertiesAvailable = record.get('hasTempValue'),
            attributesVisible = hasViewRights || hasEditRights,
            activeKeysForm = me.getActiveKeyAttributesContainer().down('property-form'),
            passiveKeysForm = me.getPassiveKeyAttributesContainer().down('property-form'),
            activeInfo = me.getKeyPreview().down('#mdc-device-security-accessor-preview-active-info'),
            passiveInfo = me.getKeyPreview().down('#mdc-device-security-accessor-preview-passive-info');

        Ext.suspendLayouts();
        me.getKeyPreview().doLoadRecord(record);
        if (actionsMenu) {
            actionsMenu.record = record;
        }

        var expDate = Ext.isEmpty(record.get('expirationTime')) || record.get('expirationTime')===0 ? '-' : Uni.DateTime.formatDateShort( new Date(record.get('expirationTime')));
        if (record.get('swapped')) {
            var swapDate = Ext.isEmpty(record.get('modificationDate')) ? '-' : Uni.DateTime.formatDateShort( new Date(record.get('modificationDate')));
            activeInfo.setInfo( Uni.I18n.translate('general.activeFromxUntily', 'MDC', "Active from {0} until {1}", [swapDate, expDate]) );
            passiveInfo.setInfo( Uni.I18n.translate('general.activeUntilx', 'MDC', "Active until {0}", swapDate) );
            activeInfo.show();
            passiveInfo.show();
        } else {
            if (expDate === '-') {
                activeInfo.hide();
            } else {
                activeInfo.setInfo( Uni.I18n.translate('general.activeUntilx', 'MDC', "Active until {0}", expDate) );
                activeInfo.show();
            }
            passiveInfo.hide();
        }
        me.getActiveKeyAttributesContainer().setVisible(attributesVisible);
        if (attributesVisible) {
            activeKeysForm.initProperties(record.currentProperties());
        }

        me.getPassiveKeyAttributesContainer().setVisible(tempPropertiesAvailable);
        if (tempPropertiesAvailable) {
            passiveKeysForm.initProperties(record.tempProperties());
        }
        if (attributesVisible || tempPropertiesAvailable) {
            var task = new Ext.util.DelayedTask(function(){
                Ext.resumeLayouts(true);
                me.hideKeyValues();
            });
            task.delay(20);
        } else {
            Ext.resumeLayouts(true);
        }

    },

    onCertificateRecordSelected: function (grid, record) {
        if (Ext.isEmpty(record)) return;
        var me = this,
            actionsMenu = me.getCertificatePreview().down('device-security-accessors-action-menu'),
            tempPropertiesAvailable = record.get('hasTempValue'),
            activeCertificatesForm = me.getActiveCertificateAttributesContainer().down('property-form'),
            passiveCertificatesForm = me.getPassiveCertificateAttributesContainer().down('property-form'),
            activeInfo = me.getCertificatePreview().down('#mdc-device-security-accessor-preview-active-info'),
            passiveInfo = me.getCertificatePreview().down('#mdc-device-security-accessor-preview-passive-info');

        Ext.suspendLayouts();
        me.getCertificatePreview().doLoadRecord(record);
        if (actionsMenu) {
            actionsMenu.record = record;
        }

        var expDate = Ext.isEmpty(record.get('expirationTime')) || record.get('expirationTime')===0 ? '-' : Uni.DateTime.formatDateShort( new Date(record.get('expirationTime')));
        if (record.get('swapped')) {
            var swapDate = Ext.isEmpty(record.get('modificationDate')) ? '-' : Uni.DateTime.formatDateShort( new Date(record.get('modificationDate')));
            activeInfo.setInfo( Uni.I18n.translate('general.activeFromxUntily', 'MDC', "Active from {0} until {1}", [swapDate, expDate]) );
            passiveInfo.setInfo( Uni.I18n.translate('general.activeUntilx', 'MDC', "Active until {0}", swapDate) );
            activeInfo.show();
            passiveInfo.show();
        } else {
            if (expDate === '-') {
                activeInfo.hide();
            } else {
                activeInfo.setInfo( Uni.I18n.translate('general.activeUntilx', 'MDC', "Active until {0}", expDate) );
                activeInfo.show();
            }
            passiveInfo.hide();
        }

        activeCertificatesForm.initProperties(record.currentProperties());
        me.getPassiveCertificateAttributesContainer().setVisible(tempPropertiesAvailable);
        if (tempPropertiesAvailable) {
            passiveCertificatesForm.initProperties(record.tempProperties());
        }
        Ext.resumeLayouts(true);
    },

    onMenuAction: function(menu, menuItem) {
        var me = this;
        console.log("CHOOOSE ACTION!!!",menuItem.action);
        switch (menuItem.action) {
            case 'editDeviceKey':
                me.navigateToEditKeyAttributes(menu.record);
                break;
            case 'editDeviceCertificate':
                me.navigateToEditCertificateAttributes(menu.record);
                break;
            case 'clearPassiveKey':
                me.clearPassive(menu.record, true);
                break;
            case 'clearPassiveCertificate':
                me.clearPassive(menu.record, false);
                break;
            case 'generatePassiveKey':
                me.generatePassiveKey(menu.record);
                break;
            case 'activatePassiveKey':
                me.activatePassiveKey(menu.record);
                break;
            case 'activatePassiveCertificate':
                me.activatePassiveCertificate(menu.record);
                break;
            case 'showKeyValues':
                me.showKeyValues(menu.record);
                break;
            case 'hideKeyValues':
                me.hideKeyValues(menu.record);
                break;
            case 'unmarkServiceKey':
                me.unmarkServiceKey(menu.record);
                break;

        }
    },

    navigateToEditKeyAttributes: function (deviceSecurityAccessorRecord) {
        this.deviceKeyRecord = deviceSecurityAccessorRecord;
        location.href = '#/devices/' + encodeURIComponent(this.deviceId) + '/keys/' + encodeURIComponent(deviceSecurityAccessorRecord.get('id')) + '/edit';
    },

    navigateToEditCertificateAttributes: function (deviceSecurityAccessorRecord) {
        this.deviceCertificateRecord = deviceSecurityAccessorRecord;
        location.href = '#/devices/' + encodeURIComponent(this.deviceId) + '/certificates/' + encodeURIComponent(deviceSecurityAccessorRecord.get('id')) + '/edit';
    },

    editDeviceKey: function(deviceId, accessorId) {
        var me = this,
            keyModel = Ext.ModelManager.getModel('Mdc.securityaccessors.model.DeviceSecurityKey');

        me.deviceId = deviceId;
        keyModel.getProxy().setUrl(encodeURIComponent(deviceId));

        if (Ext.isEmpty(me.deviceKeyRecord)) {


            keyModel.load(accessorId, {
                success: function (keyRecord) {
                    me.deviceKeyRecord = keyRecord;
                    me.doEditDeviceKey(deviceId, accessorId);
                }
            });
        } else {
            me.doEditDeviceKey(deviceId, accessorId);
        }
    },

    doEditDeviceKey: function(deviceId, accessorId) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0];

        viewport.setLoading();
        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceId, {
            success: function (device) {
                var widget = Ext.create('Mdc.securityaccessors.view.EditDeviceKeyAttributes', {
                    device: device,
                    keyRecord: me.deviceKeyRecord
                });
                me.getApplication().fireEvent('changecontentevent', widget);
                widget.down('form').setTitle( Uni.I18n.translate('general.editx', 'MDC', "Edit '{0}'", me.deviceKeyRecord.get('name')) );
                me.getApplication().fireEvent('deviceKeyLoaded', me.deviceKeyRecord);
                me.getApplication().fireEvent('loadDevice', device);

                me.getEditActiveKeyAttributesContainer().setVisible(me.deviceKeyRecord.currentPropertiesStore.data.items.length > 0);
                me.getEditActiveKeyAttributesContainer().down('property-form').initProperties(me.deviceKeyRecord.currentProperties());
                var tempPropertiesAvailable = me.deviceKeyRecord.tempPropertiesStore.data.items.length > 0;
                me.getEditPassiveKeyAttributesContainer().setVisible(tempPropertiesAvailable);
                if (tempPropertiesAvailable) {
                    me.getEditPassiveKeyAttributesContainer().down('property-form').initProperties(me.deviceKeyRecord.tempProperties());
                }
                viewport.setLoading(false);
            }
        });
    },

    editDeviceCertificate: function(deviceId, accessorId) {
        var me = this,
            certificateModel = Ext.ModelManager.getModel('Mdc.securityaccessors.model.DeviceSecurityCertificate');

        me.deviceId = deviceId;
        certificateModel.getProxy().setUrl(encodeURIComponent(deviceId));

        if (Ext.isEmpty(me.deviceCertificateRecord)) {
            certificateModel.load(accessorId, {
                success: function (certificateRecord) {
                    me.deviceCertificateRecord = certificateRecord;
                    me.doEditDeviceCertificate(deviceId, accessorId);
                }
            });
        } else {
            me.doEditDeviceCertificate(deviceId, accessorId);
        }
    },

    doEditDeviceCertificate: function(deviceId, accessorId) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0];

        viewport.setLoading();
        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceId, {
            success: function (device) {
                var widget = Ext.create('Mdc.securityaccessors.view.EditDeviceCertificateAttributes', {
                    device: device,
                    certificateRecord: me.deviceCertificateRecord
                });
                me.getApplication().fireEvent('changecontentevent', widget);
                widget.down('form').setTitle( Uni.I18n.translate('general.editx', 'MDC', "Edit '{0}'", me.deviceCertificateRecord.get('name')) );
                me.getApplication().fireEvent('deviceCertificateLoaded', me.deviceCertificateRecord);
                me.getApplication().fireEvent('loadDevice', device);

                var propStore = me.deviceCertificateRecord.currentProperties(),
                    attrCount = propStore.getCount(),
                    propRecord = undefined,
                    activeAliasCombo = undefined,
                    passiveAliasCombo = undefined,
                    aliasesStore = Ext.getStore('Mdc.securityaccessors.store.CertificateAliases') || Ext.create('Mdc.securityaccessors.store.CertificateAliases'),
                    trustStoreField = undefined,
                    passiveTrustStoreField = undefined,
                    trustStoreId = undefined,
                    trustStoreName = undefined;

                delete aliasesStore.getProxy().extraParams['trustStore'];
                if (attrCount>0) {
                    for (var i=0; i<attrCount; i++) {
                        propRecord = propStore.getAt(i);
                        if (propRecord.raw.key === 'alias') {
                            aliasesStore.getProxy().setUrl(propRecord.raw.propertyTypeInfo.propertyValuesResource.possibleValuesURI);
                            activeAliasCombo = {
                                xtype: 'combobox',
                                fieldLabel: propRecord.raw.name,
                                labelWidth: 200,
                                width: 456,
                                itemId: 'mdc-active-alias-combo',
                                dataIndex: 'alias',
                                emptyText: Uni.I18n.translate('general.startTypingToSelect', 'MDC', 'Start typing to select...'),
                                listConfig: {
                                    emptyText: Uni.I18n.translate('general.startTypingToSelect', 'MDC', 'Start typing to select...')
                                },
                                displayField: 'alias',
                                value: propRecord.raw.propertyValueInfo.value,
                                valueField: 'alias',
                                store: aliasesStore,
                                queryMode: 'remote',
                                queryParam: 'alias',
                                queryDelay: 500,
                                queryCaching: false,
                                minChars: 0,
                                loadStore: false,
                                forceSelection: false,
                                listeners: {
                                    expand: {
                                        fn: me.comboLimitNotification
                                    }
                                }
                            };
                        } else if (propRecord.raw.key === 'trustStore') {
                            aliasesStore.getProxy().setExtraParam('trustStore', propRecord.raw.propertyValueInfo.value.id);
                            trustStoreField = {
                                xtype: 'displayfield',
                                fieldLabel: propRecord.raw.name,
                                value: propRecord.raw.propertyValueInfo.value ? propRecord.raw.propertyValueInfo.value.name : '-'
                            };
                            trustStoreId = propRecord.raw.propertyValueInfo.value ? propRecord.raw.propertyValueInfo.value.id : undefined;
                            trustStoreName = propRecord.raw.propertyValueInfo.value ? propRecord.raw.propertyValueInfo.value.name : undefined;
                        }
                    }
                    if (trustStoreField) {
                        me.getEditActiveCertificateAttributesContainer().add(trustStoreField);
                    }
                    me.getEditActiveCertificateAttributesContainer().add(activeAliasCombo);
                }

                propStore = me.deviceCertificateRecord.tempProperties();
                attrCount = propStore.getCount();
                propRecord = undefined;
                aliasesStore = Ext.getStore('Mdc.securityaccessors.store.CertificateAliases') || Ext.create('Mdc.securityaccessors.store.CertificateAliases');
                delete aliasesStore.getProxy().extraParams['trustStore'];

                if (attrCount>0) {
                    for (var i=0; i<attrCount; i++) {
                        propRecord = propStore.getAt(i);
                        if (propRecord.raw.key === 'alias') {
                            aliasesStore.getProxy().setUrl(propRecord.raw.propertyTypeInfo.propertyValuesResource.possibleValuesURI);
                            passiveAliasCombo = {
                                xtype: 'combobox',
                                fieldLabel: propRecord.raw.name,
                                labelWidth: 200,
                                width: 456,
                                itemId: 'mdc-passive-alias-combo',
                                dataIndex: 'alias',
                                emptyText: Uni.I18n.translate('general.startTypingToSelect', 'MDC', 'Start typing to select...'),
                                listConfig: {
                                    emptyText: Uni.I18n.translate('general.startTypingToSelect', 'MDC', 'Start typing to select...')
                                },
                                displayField: 'alias',
                                valueField: 'alias',
                                value: propRecord.raw.propertyValueInfo.value,
                                store: aliasesStore,
                                queryMode: 'remote',
                                queryParam: 'alias',
                                queryDelay: 500,
                                queryCaching: false,
                                minChars: 0,
                                loadStore: false,
                                forceSelection: false,
                                listeners: {
                                    expand: {
                                        fn: me.comboLimitNotification
                                    }
                                }
                            };
                        } else if (propRecord.raw.key === 'trustStore') {
                            if (trustStoreId && trustStoreName) {
                                if (!propRecord.raw.propertyValueInfo.value) {
                                    propRecord.raw.propertyValueInfo.value = {};
                                }
                                if (propRecord.raw.propertyValueInfo.value) {
                                    propRecord.raw.propertyValueInfo.value.id = trustStoreId;
                                    propRecord.raw.propertyValueInfo.value.name = trustStoreName;
                                }
                            }
                            aliasesStore.getProxy().setExtraParam('trustStore', propRecord.raw.propertyValueInfo.value.id);
                            passiveTrustStoreField = {
                                xtype: 'displayfield',
                                fieldLabel: propRecord.raw.name,
                                value: propRecord.raw.propertyValueInfo.value ? propRecord.raw.propertyValueInfo.value.name : '-'
                            };
                        }
                    }
                    if (passiveTrustStoreField) {
                        me.getEditPassiveCertificateAttributesContainer().add(passiveTrustStoreField);
                    }
                    me.getEditPassiveCertificateAttributesContainer().add(passiveAliasCombo);
                }

                viewport.setLoading(false);
            }
        });
    },

    saveKeyAttributes: function() {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            activePropsForm = me.getEditActiveKeyAttributesContainer().down('#mdc-device-key-attributes-edit-active-attributes-property-form'),
            passivePropsForm = me.getEditPassiveKeyAttributesContainer().down('#mdc-device-key-attributes-edit-passive-attributes-property-form'),
            errorMsgPnl = me.getEditDeviceKeyPanel().down('uni-form-error-message'),
            key,
            field,
            raw;

        viewport.setLoading();
        errorMsgPnl.hide();
        me.deviceKeyRecord.beginEdit();
        raw = activePropsForm.getFieldValues();
        me.deviceKeyRecord.currentProperties().each(function (property) {
            key = property.get('key');
            field = activePropsForm.getPropertyField(key);
            if (field !== undefined) {
                var value = field.getValue(raw);
                propertyValue = property.getPropertyValue();
                propertyValue.set('value', value);
                propertyValue.set('propertyHasValue', !Ext.isEmpty(value));
            }
        });
        raw = passivePropsForm.getFieldValues();
        me.deviceKeyRecord.tempProperties().each(function (property) {
            key = property.get('key');
            field = passivePropsForm.getPropertyField(key);
            if (field !== undefined) {
                var value = field.getValue(raw);
                propertyValue = property.getPropertyValue();
                propertyValue.set('value', value);
                propertyValue.set('propertyHasValue', !Ext.isEmpty(value));
            }
        });
        me.deviceKeyRecord.endEdit();
        me.deviceKeyRecord.getProxy().setUrl(me.deviceId);
        me.deviceKeyRecord.save({
            success: function () {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('general.key.saved', 'MDC', 'Key saved'));
                me.navigateToKeysOverview();
            },
            failure: function(record, operation) {
                var json = Ext.decode(operation.response.responseText, true);
                if (json && json.errors) {
                    Ext.each(json.errors, function (error) {
                        var parts = error.id.split('.');
                        if (parts[0] === 'currentProperties') {
                            me.deviceKeyRecord.currentProperties().each(function (property) {
                                if (parts[1] === property.get('key')) {
                                    activePropsForm.down('[fieldLabel=' + property.name + ']').markInvalid(error.msg);
                                    return false;
                                }
                            });
                        } else if (parts[0] === 'tempProperties') {
                            me.deviceKeyRecord.tempProperties().each(function (property) {
                                if (parts[1] === property.get('key')) {
                                    passivePropsForm.down('[fieldLabel='+property.name+']').markInvalid(error.msg);
                                    return false;
                                }
                            });
                        }
                    });
                    errorMsgPnl.show();
                }
            },
            callback: function () {
                viewport.setLoading(false);
            }
        });
    },

    saveCertificateAttributes: function() {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            activeAliasCombo = me.getEditActiveCertificateAttributesContainer().down('#mdc-active-alias-combo'),
            passiveAliasCombo = me.getEditPassiveCertificateAttributesContainer().down('#mdc-passive-alias-combo'),
            errorMsgPnl = me.getEditDeviceCertificatePanel().down('uni-form-error-message'),
            key,
            value,
            trustStoreId = undefined,
            trustStoreName = undefined,
            aliasHasValue = false;

        viewport.setLoading();
        errorMsgPnl.hide();
        me.deviceCertificateRecord.beginEdit();

        me.deviceCertificateRecord.currentProperties().each(function (property) {
            key = property.get('key');
            if (key === 'alias') {
                value = activeAliasCombo.getValue();
                aliasHasValue = !Ext.isEmpty(value);
                propertyValue = property.getPropertyValue();
                propertyValue.set('value', value);
                propertyValue.set('propertyHasValue', aliasHasValue);
            } else if (key === 'trustStore') {
                trustStoreId = property.raw.propertyValueInfo.value.id;
                trustStoreName = property.raw.propertyValueInfo.value.name;
            }
        });
        me.deviceCertificateRecord.currentProperties().each(function (property) {
            key = property.get('key');
            if (key === 'trustStore' && !aliasHasValue) {
                propertyValue = property.getPropertyValue();
                propertyValue.set('value', null);
                propertyValue.set('propertyHasValue', false);
            }
        });

        aliasHasValue = false;
        me.deviceCertificateRecord.tempProperties().each(function (property) {
            key = property.get('key');
            if (key === 'alias') {
                value = passiveAliasCombo.getValue();
                aliasHasValue = !Ext.isEmpty(value);
                propertyValue = property.getPropertyValue();
                propertyValue.set('value', value);
                propertyValue.set('propertyHasValue', aliasHasValue);
            }
        });
        me.deviceCertificateRecord.tempProperties().each(function (property) {
            key = property.get('key');
            if (key === 'trustStore' && trustStoreId && trustStoreName && aliasHasValue) {
                propertyValue = property.getPropertyValue();
                propertyValue.set('value', {id: trustStoreId, name: trustStoreName});
                propertyValue.set('propertyHasValue', true);
            } else if (key === 'trustStore' && !aliasHasValue) {
                propertyValue = property.getPropertyValue();
                propertyValue.set('value', null);
                propertyValue.set('propertyHasValue', false);
            }
        });

        me.deviceCertificateRecord.endEdit();
        me.deviceCertificateRecord.save({
            success: function () {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('general.certificate.saved', 'MDC', 'Certificate saved'));
                me.navigateToCertificatesOverview();
            },
            failure: function(record, operation) {
                var json = Ext.decode(operation.response.responseText, true);
                if (json && json.errors) {
                    Ext.each(json.errors, function (error) {
                        var parts = error.id.split('.');
                        if (parts[0] === 'currentProperties') {
                            me.deviceCertificateRecord.currentProperties().each(function (property) {
                                if (parts[1] === 'alias') {
                                    activeAliasCombo.markInvalid(error.msg);
                                    return false;
                                }
                            });
                        } else if (parts[0] === 'tempProperties') {
                            me.deviceCertificateRecord.tempProperties().each(function (property) {
                                if (parts[1] === 'alias') {
                                    passiveAliasCombo.markInvalid(error.msg);
                                    return false;
                                }
                            });
                        }
                    });
                    errorMsgPnl.show();
                }
            },
            callback: function () {
                viewport.setLoading(false);
            }
        });
    },

    clearPassive: function(keyOrCertificateRecord, keyMode) {
        var me = this,
            url = '/api/ddr/devices/{deviceId}/securityaccessors/keys/{keyOrCertificateId}/temp',
            title,
            confirmMessage,
            clearedMessage;

        if (keyMode) {
            title = Uni.I18n.translate('general.clearPassiveKey.title', 'MDC', "Clear passive key of '{0}'?", keyOrCertificateRecord.get('name'));
            confirmMessage = Uni.I18n.translate('general.clearPassiveKey.msg', 'MDC', 'The passive key will no longer be available.');
            clearedMessage = Uni.I18n.translate('general.passiveKey.cleared', 'MDC', 'Passive key cleared');
        } else {
            title = Uni.I18n.translate('general.clearPassiveCertificate.title', 'MDC', "Clear passive certificate of '{0}'?", keyOrCertificateRecord.get('name'));
            confirmMessage = Uni.I18n.translate('general.clearPassiveCertificate.msg', 'MDC', 'The passive certificate will no longer be available.');
            clearedMessage = Uni.I18n.translate('general.passiveCertificate.cleared', 'MDC', 'Passive certificate cleared');
        }

        Ext.create('Uni.view.window.Confirmation', {confirmText: Uni.I18n.translate('general.clear', 'MDC', 'Clear')}).show({
            title: title,
            msg: confirmMessage,
            fn: function (action) {
                if (action == 'confirm') {
                    url = url.replace('{deviceId}', me.deviceId).replace('{keyOrCertificateId}', keyOrCertificateRecord.get('id'));
                    Ext.Ajax.request({
                        url: url,
                        method: 'DELETE',
                        jsonData: Ext.encode(keyOrCertificateRecord.getData()),
                        success: function () {
                            me.getApplication().fireEvent('acknowledge', clearedMessage);
                            var router = me.getController('Uni.controller.history.Router'),
                                splittedPath = router.currentRoute.split('/');
                            splittedPath.pop();
                            router.getRoute(splittedPath.join('/') + '/' + (keyMode ? 'keys' : 'certificates')).forward(router.arguments);
                        }
                    });
                }
            }
        });
    },

    generatePassiveKey: function(keyRecord) {
        var me = this,
            url = '/api/ddr/devices/{deviceId}/securityaccessors/keys/{keyId}/temp';

        url = url.replace('{deviceId}', me.deviceId).replace('{keyId}', keyRecord.get('id'));

        Ext.Ajax.request({
            url: url,
            method: 'POST',
            jsonData: Ext.encode(keyRecord.getData()),
            success: function () {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('general.passiveKey.generated', 'MDC', 'Passive key generated'));
                var router = me.getController('Uni.controller.history.Router'),
                    splittedPath = router.currentRoute.split('/');
                splittedPath.pop();
                router.getRoute(splittedPath.join('/') + '/' + 'keys').forward(router.arguments);
            }
        });
    },

    activatePassiveKey: function(keyRecord) {
        var me = this,
            url = '/api/ddr/devices/{deviceId}/securityaccessors/keys/{keyId}/swap';

        url = url.replace('{deviceId}', me.deviceId).replace('{keyId}', keyRecord.get('id'));

        Ext.Ajax.request({
            url: url,
            method: 'PUT',
            jsonData: Ext.encode(keyRecord.getData()),
            success: function () {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('general.passiveKey.activated', 'MDC', 'Passive key activated'));
                var router = me.getController('Uni.controller.history.Router'),
                    splittedPath = router.currentRoute.split('/');
                splittedPath.pop();
                router.getRoute(splittedPath.join('/') + '/' + 'keys').forward(router.arguments);
            }
        });
    },

    activatePassiveCertificate: function(certificateRecord) {
        var me = this,
            url = '/api/ddr/devices/{deviceId}/securityaccessors/certificates/{certificateId}/swap';

        url = url.replace('{deviceId}', me.deviceId).replace('{certificateId}', certificateRecord.get('id'));

        Ext.Ajax.request({
            url: url,
            method: 'PUT',
            jsonData: Ext.encode(certificateRecord.getData()),
            success: function () {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('general.passiveCertificate.activated', 'MDC', 'Passive certificate activated'));
                var router = me.getController('Uni.controller.history.Router'),
                    splittedPath = router.currentRoute.split('/');
                splittedPath.pop();
                router.getRoute(splittedPath.join('/') + '/' + 'certificates').forward(router.arguments);
            }
        });
    },

    showKeyValues: function() {
        var me = this;

        me.getKeyPreview().down('device-security-accessor-preview-form #mdc-device-security-accessor-preview-active-attributes-container property-form').showValues();
        me.getKeyPreview().down('device-security-accessor-preview-form #mdc-device-security-accessor-preview-passive-attributes-container property-form').showValues();

        var previewActionsMenu = me.getKeyPreview().down('device-security-accessors-action-menu');
        previewActionsMenu.down('#mdc-device-security-accessors-action-menu-item-show-hide').action = 'hideKeyValues';
        previewActionsMenu.down('#mdc-device-security-accessors-action-menu-item-show-hide').setText(Uni.I18n.translate('general.hideValues', 'MDC', 'Hide values'));

        var gridActionsMenu = me.getKeysGrid().down('uni-actioncolumn').menu;
        gridActionsMenu.down('#mdc-device-security-accessors-action-menu-item-show-hide').action = 'hideKeyValues';
        gridActionsMenu.down('#mdc-device-security-accessors-action-menu-item-show-hide').setText(Uni.I18n.translate('general.hideValues', 'MDC', 'Hide values'));
    },

    hideKeyValues: function() {
        var me = this;

        me.getKeyPreview().down('device-security-accessor-preview-form #mdc-device-security-accessor-preview-active-attributes-container property-form').hideValues();
        me.getKeyPreview().down('device-security-accessor-preview-form #mdc-device-security-accessor-preview-passive-attributes-container property-form').hideValues();

        var previewActionsMenu = me.getKeyPreview().down('device-security-accessors-action-menu');
        previewActionsMenu.down('#mdc-device-security-accessors-action-menu-item-show-hide').action = 'showKeyValues';
        previewActionsMenu.down('#mdc-device-security-accessors-action-menu-item-show-hide').setText(Uni.I18n.translate('general.showValues', 'MDC', 'Show values'));

        var gridActionsMenu = me.getKeysGrid().down('uni-actioncolumn').menu;
        gridActionsMenu.down('#mdc-device-security-accessors-action-menu-item-show-hide').action = 'showKeyValues';
        gridActionsMenu.down('#mdc-device-security-accessors-action-menu-item-show-hide').setText(Uni.I18n.translate('general.showValues', 'MDC', 'Show values'));
    },

    unmarkServiceKey: function(deviceSecurityAccessorRecord) {
            var me = this;
            viewport = Ext.ComponentQuery.query('viewport')[0];

            me.deviceKeyRecord = deviceSecurityAccessorRecord;
            me.deviceKeyRecord.set('serviceKey', false);

            viewport.setLoading();

            var url = '/api/ddr/devices/{deviceId}/securityaccessors/keys/{keyId}/unmarkservicekey';



            url = url.replace('{deviceId}', me.deviceId).replace('{keyId}', me.deviceKeyRecord.get('id'));

            Ext.Ajax.request({
                url: url,
                method: 'PUT',
                jsonData: Ext.encode(deviceSecurityAccessorRecord.getData()),
                success: function () {
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('general.serviceKey.unmarked', 'MDC', 'Service key unmarked'));
                },
                callback: function () {
                    viewport.setLoading(false);
                }
            });

    },



    comboLimitNotification: function (combo) {
        var picker = combo.getPicker(),
            fn = function (view) {
                var store = view.getStore(),
                    el = view.getEl().down('.' + Ext.baseCSSPrefix + 'list-plain');
                if (store.getTotalCount() > store.getCount()) {
                    el.appendChild({
                        tag: 'li',
                        html: Uni.I18n.translate('issues.limitNotification', 'MDC', 'Keep typing to narrow down'),
                        cls: Ext.baseCSSPrefix + 'boundlist-item combo-limit-notification'
                    });
                }
            };

        picker.on('refresh', fn);
        picker.on('beforehide', function () {
            picker.un('refresh', fn);
        }, combo, {single: true});
    }

});