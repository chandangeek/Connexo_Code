/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.controller.DeviceSecurityAccessors', {
    extend: 'Ext.app.Controller',

    stores: [
        'Mdc.securityaccessors.store.DeviceSecurityKeys',
        'Mdc.securityaccessors.store.DeviceSecurityCertificates'
    ],

    models: [
        'Mdc.securityaccessors.model.DeviceSecurityKey',
        'Mdc.securityaccessors.model.DeviceSecurityCertificate'
    ],

    views: [
        'Mdc.securityaccessors.view.DeviceSecurityAccessorsOverview',
        'Mdc.securityaccessors.view.EditDeviceKeyAttributes',
        'Mdc.securityaccessors.view.EditDeviceCertificateAttributes'
    ],

    refs: [
        {
            ref: 'keyPreviewForm',
            selector: '#mdc-device-accessors-key-preview device-security-accessor-preview-form form'
        },
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
        } else if (newActiveItem.itemId === 'mdc-device-accessors-certificates-tab') {
            url = url.replace('/keys', '/certificates');
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
        var me = this,
            actionsMenu = me.getKeyPreview().down('device-security-accessors-action-menu');

        me.getKeyPreviewForm().loadRecord(record);
        me.getKeyPreview().setTitle(Ext.htmlEncode(record.get('name')));
        if (actionsMenu) {
            actionsMenu.record = record;
        }

        var expDate = Ext.isEmpty(record.get('expirationTime')) ? '-' : Uni.DateTime.formatDateShort( new Date(record.get('expirationTime')));
        if (record.get('swapped')) {
            var swapDate = Ext.isEmpty(record.get('modificationDate')) ? '-' : Uni.DateTime.formatDateShort( new Date(record.get('modificationDate')));
            me.getKeyPreview().down('#mdc-device-security-accessor-preview-active-info').setInfo(
                Uni.I18n.translate('general.activeFromxUntily', 'MDC', "Active from {0} until {1}", [swapDate, expDate])
            );
            me.getKeyPreview().down('#mdc-device-security-accessor-preview-passive-info').setInfo(
                Uni.I18n.translate('general.activeUntilx', 'MDC', "Active until {0}", swapDate)
            );
        } else {
            me.getKeyPreview().down('#mdc-device-security-accessor-preview-active-info').setInfo(
                Uni.I18n.translate('general.activeUntilx', 'MDC', "Active until {0}", expDate)
            );
            me.getKeyPreview().down('#mdc-device-security-accessor-preview-passive-info').clearInfo();
        }
        me.getActiveKeyAttributesContainer().setVisible(record.currentPropertiesStore.data.items.length > 0);
        me.getActiveKeyAttributesContainer().down('property-form').initProperties(record.currentProperties());
        var tempPropertiesAvailable = record.tempPropertiesStore.data.items.length > 0;
        me.getPassiveKeyAttributesContainer().setVisible(tempPropertiesAvailable);
        if (tempPropertiesAvailable) {
            me.getPassiveKeyAttributesContainer().down('property-form').initProperties(record.tempProperties());
        }
    },

    onCertificateRecordSelected: function (grid, record) {
        var me = this,
            actionsMenu = me.getCertificatePreview().down('device-security-accessors-action-menu');

        me.getCertificatePreviewForm().loadRecord(record);
        me.getCertificatePreview().setTitle(Ext.htmlEncode(record.get('name')));
        if (actionsMenu) {
            actionsMenu.record = record;
        }

        var expDate = Ext.isEmpty(record.get('expirationTime')) ? '-' : Uni.DateTime.formatDateShort( new Date(record.get('expirationTime')));
        if (record.get('swapped')) {
            var swapDate = Ext.isEmpty(record.get('modificationDate')) ? '-' : Uni.DateTime.formatDateShort( new Date(record.get('modificationDate')));
            me.getCertificatePreview().down('#mdc-device-security-accessor-preview-active-info').setInfo(
                Uni.I18n.translate('general.activeFromxUntily', 'MDC', "Active from {0} until {1}", [swapDate, expDate])
            );
            me.getCertificatePreview().down('#mdc-device-security-accessor-preview-passive-info').setInfo(
                Uni.I18n.translate('general.activeUntilx', 'MDC', "Active until {0}", swapDate)
            );
        } else {
            me.getCertificatePreview().down('#mdc-device-security-accessor-preview-active-info').setInfo(
                Uni.I18n.translate('general.activeUntilx', 'MDC', "Active until {0}", expDate)
            );
            me.getCertificatePreview().down('#mdc-device-security-accessor-preview-passive-info').clearInfo();
        }

        me.getActiveCertificateAttributesContainer().setVisible(record.currentPropertiesStore.data.items.length > 0);
        me.getActiveCertificateAttributesContainer().down('property-form').initProperties(record.currentProperties());
        var tempPropertiesAvailable = record.tempPropertiesStore.data.items.length > 0;
        me.getPassiveCertificateAttributesContainer().setVisible(tempPropertiesAvailable);
        if (tempPropertiesAvailable) {
            me.getPassiveCertificateAttributesContainer().down('property-form').initProperties(record.tempProperties());
        }
    },

    onMenuAction: function(menu, menuItem) {
        var me = this;

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
                    certificateRecord: me.certificateKeyRecord
                });
                me.getApplication().fireEvent('changecontentevent', widget);
                widget.down('form').setTitle( Uni.I18n.translate('general.editx', 'MDC', "Edit '{0}'", me.deviceCertificateRecord.get('name')) );
                me.getApplication().fireEvent('deviceCertificateLoaded', me.deviceCertificateRecord);
                me.getApplication().fireEvent('loadDevice', device);

                me.getEditActiveCertificateAttributesContainer().setVisible(me.deviceCertificateRecord.currentPropertiesStore.data.items.length > 0);
                me.getEditActiveCertificateAttributesContainer().down('property-form').initProperties(me.deviceCertificateRecord.currentProperties());
                var tempPropertiesAvailable = me.deviceCertificateRecord.tempPropertiesStore.data.items.length > 0;
                me.getEditPassiveCertificateAttributesContainer().setVisible(tempPropertiesAvailable);
                if (tempPropertiesAvailable) {
                    me.getEditPassiveCertificateAttributesContainer().down('property-form').initProperties(me.deviceCertificateRecord.tempProperties());
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
            activePropsForm = me.getEditActiveCertificateAttributesContainer().down('#mdc-device-certificate-attributes-edit-active-attributes-property-form'),
            passivePropsForm = me.getEditPassiveCertificateAttributesContainer().down('#mdc-device-certificate-attributes-edit-passive-attributes-property-form'),
            errorMsgPnl = me.getEditDeviceCertificatePanel().down('uni-form-error-message'),
            key,
            field,
            raw;

        viewport.setLoading();
        errorMsgPnl.hide();
        me.deviceCertificateRecord.beginEdit();
        raw = activePropsForm.getFieldValues();
        me.deviceCertificateRecord.currentProperties().each(function (property) {
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
        me.deviceCertificateRecord.tempProperties().each(function (property) {
            key = property.get('key');
            field = passivePropsForm.getPropertyField(key);
            if (field !== undefined) {
                var value = field.getValue(raw);
                propertyValue = property.getPropertyValue();
                propertyValue.set('value', value);
                propertyValue.set('propertyHasValue', !Ext.isEmpty(value));
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
                                if (parts[1] === property.get('key')) {
                                    activePropsForm.down('[fieldLabel=' + property.name + ']').markInvalid(error.msg);
                                    return false;
                                }
                            });
                        } else if (parts[0] === 'tempProperties') {
                            me.deviceCertificateRecord.tempProperties().each(function (property) {
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
    }

});