/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.devicefirmware.controller.DeviceFirmware', {
    extend: 'Ext.app.Controller',

    views: [
        'Fwc.devicefirmware.view.Setup',
        'Fwc.devicefirmware.view.Upload',
        'Fwc.devicefirmware.view.FirmwareForm',
        'Fwc.devicefirmware.view.ConfirmActivateVersionWindow',
    ],

    requires: [
        'Mdc.model.Device',
        'Uni.util.Common',
        'Uni.view.window.Confirmation'
    ],

    models: [
        'Mdc.model.Device',
        'Fwc.devicefirmware.model.FirmwareMessage',
        'Fwc.devicefirmware.model.FirmwareMessageSpec',
        'Fwc.devicefirmware.model.DeviceFirmwareHistoryModel'
    ],

    stores: [
        'Fwc.devicefirmware.store.Firmwares',
        'Fwc.devicefirmware.store.FirmwareActions',
        'Fwc.devicefirmware.store.DeviceFirmwareHistoryStore'
    ],

    refs: [
        {ref: 'container', selector: 'viewport > #contentPanel'},
        {ref: 'setupPage', selector: 'device-firmware-setup'},
        {ref: 'uploadPage', selector: 'device-firmware-upload'},
        {ref: 'uploadForm', selector: 'device-firmware-upload-form'}
    ],

    init: function () {
        this.control({
            'device-firmware-setup device-firmware-action-menu': {
                click: function (menu, item) {
                    if (item.action === 'run' || item.action === 'runnow') {
                        this.doRun(item.record, item.action);
                    } else {
                        var firmwareType = menu.up('form').record.getAssociatedData().firmwareType.id;
                        this.getController('Uni.controller.history.Router')
                            .getRoute('devices/device/firmware/upload')
                            .forward(null, {action: item.action, firmwareType: firmwareType});
                    }
                }
            },
            'device-firmware-setup button[action=cancelUpgrade]': {
                click: this.doCancelUpgrade
            },
            'device-firmware-setup button[action=retry]': {
                click: this.doRetry
            },
            'device-firmware-setup button[action=check]': {
                click: this.doRetry
            },
            'device-firmware-setup button[action=viewDeviceEvents]': {
                click: function () {
                    this.getController('Uni.controller.history.Router')
                        .getRoute('devices/device/events')
                        .forward();
                }
            },
            'device-firmware-setup button[action=viewLog]': {
                click: function (el) {
                    var record = el.up('#message-failed').record;
                    this.getController('Uni.controller.history.Router')
                        .getRoute('devices/device/firmware/log')
                        .forward({firmwareId: record.get('firmwareVersionId')}, {
                            firmwareComTaskId: record.get('firmwareComTaskId'),
                            firmwareComTaskSessionId: record.get('firmwareComTaskSessionId')
                        });
                }
            },
            '#device-firmware-upload-form button[action=uploadFirmware]': {
                click: this.uploadFirmware
            },
            'device-firmware-setup button[action=activateVersion]': {
                click: this.doActivateVersion
            },
            '#history-grid-action-menu': {
                click: this.chooseAction
            }
        });
    },

    forceUpload: function(record, container, router){
        var me = this;
        var errorMsg = me.getUploadPage().down('#form-errors');
        record.getProxy().setExtraParam('force', true);
        record.save({
                    success: function () {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceFirmware.upgrade.success', 'FWC', 'Firmware upload scheduled'));
                        container.setLoading(false);
                        router.getRoute('devices/device/firmware').forward();
                    },
                    failure: function (record, resp) {
                        errorMsg.show();
                        container.setLoading(false);
                    }
        });
    },

    uploadFirmware: function () {
        var me = this,
            uploadPage = me.getUploadPage(),
            errorMsg = uploadPage.down('#form-errors'),
            router = me.getController('Uni.controller.history.Router'),
            propertyForm = uploadPage.down('property-form'),
            messageSpec = propertyForm.getRecord(),
            model = Ext.ModelManager.getModel('Fwc.devicefirmware.model.FirmwareMessage'),
            releaseDate = uploadPage.down('#uploadFileField').getValue(),
            timestamp = releaseDate && releaseDate.getTime(),
            container = me.getContainer(),
            record;

        if (!propertyForm.isValid()) {
            errorMsg.show();
            return;
        }
        container.setLoading();
        errorMsg.hide();
        propertyForm.clearInvalid();

        model.getProxy().setExtraParam('deviceId', Uni.util.Common.decodeURIArguments(router.arguments.deviceId));

        record = Ext.create(model, {
            uploadOption: messageSpec.get('id'),
            localizedValue: messageSpec.get('localizedValue'),
            releaseDate: timestamp
        });

        propertyForm.updateRecord();
        record.propertiesStore = messageSpec.properties();

        record.getProxy().setExtraParam('force', false);

        record.save({
            success: function () {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceFirmware.upgrade.success', 'FWC', 'Firmware upload scheduled'));
                container.setLoading(false);
                router.getRoute('devices/device/firmware').forward();
            },
            failure: function (record, resp) {
                var response = resp.response;
                if (response.status == 400) {
                    var responseText = Ext.decode(response.responseText, true);
                    if (responseText && responseText.errors) {
                        var errorsArr = [];
                        var canFurtherUpload = responseText.confirmation;

                        var confirmationWindow = Ext.create('Uni.view.window.Confirmation',{
                            confirmText: 'Upload',
                            confirmation: function () {
                                 uploadPage.setLoading(true);
                                 this.hide();
                                 me.forceUpload(record, container, router);
                            }
                        });

                        Ext.each(responseText.errors, function (error) {
                            var errorId = error.id;
                            if ( !canFurtherUpload ){
                                var errorKeyArr = errorId.split('.');
                                errorKeyArr.shift(); // remove first item, as it is not presented in property
                                errorId = errorKeyArr.join('.')
                            } else {
                                errorId = error.title;
                            }
                            errorsArr.push({id: errorId, msg: error.msg});
                        });
                        if (canFurtherUpload && errorsArr && errorsArr.length){
                           var fieldContainer = Ext.create('Ext.form.FieldContainer');
                           var htmlText = Uni.I18n.translate('deviceFirmware.upgrade.somefirmwarechecks', 'FWC', 'Some firmware version checks have been unsuccessful:') + '<br><br>';
                           Ext.each(errorsArr, function (error) {
                                htmlText += ('<b>' + error['id'] + '</b><br><br>');
                                htmlText += (' -' + error['msg'] + '<br><br>');
                           });
                           fieldContainer.add({
                                xtype: 'displayfield',
                                htmlEncode: false,
                                itemId: 'errorsText',
                                value: htmlText,
                                padding: '0 50'
                            });
                            confirmationWindow.insert(1, fieldContainer);
                            confirmationWindow.show({
                                  title: 'Upload firmware?',
                            });
                        } else {
                            var errorsWithoutId = '',
                                code = '';
                            if (responseText && responseText.errorCode) {
                                code = responseText.errorCode;
                            }
                            var foundMessagesWithoutId = false;
                            Ext.each(errorsArr, function (error) {
                                if (Ext.isEmpty(error['id']) && !Ext.isEmpty(error['msg'])) {
                                    foundMessagesWithoutId = true;
                                    errorsWithoutId = errorsWithoutId + error['msg'];
                                }
                            });
                            if (foundMessagesWithoutId) {
                                me.getApplication().getController('Uni.controller.Error').showError(Uni.I18n.translate('deviceFirmware.upgrade.errors.title', 'FWC', 'Couldn\'t perform your action'), Uni.I18n.translate('deviceFirmware.upgrade.errors', 'FWC', 'Firmware upload failed!') + '.' + errorsWithoutId, code);
                            } else {
                                errorMsg.show();
                                propertyForm.markInvalid(errorsArr);
                            }
                        }
                        uploadPage.setLoading(false);
                    }
                    container.setLoading(false);
                }
            }
        });
    },

    doRun: function (record, action) {
        var me = this,
            container = me.getContainer(),
            router = me.getController('Uni.controller.history.Router');

        container.setLoading();
        Ext.Ajax.request({
            isNotEdit: true,
            method: 'PUT',
            url: '/api/fwc/devices/{deviceId}/status/{action}'
                .replace('{action}', action)
                .replace('{deviceId}', Uni.util.Common.encodeURIComponent(router.arguments.deviceId))
                .replace('{id}', record.get('comTaskId')),
            jsonData: _.pick(container.device.getData(), 'version'),
            success: function () {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceFirmware.upgrade', 'FWC', 'The firmware version is being read. Actual firmware version information will be available as soon as the action has completed.'));
                router.getRoute().forward();
            },
            callback: function () {
                container.setLoading(false);
            }
        });
    },

    doRetry: function (btn) {
        var me = this,
            form = btn.up('form'),
            record = form.down('#message-failed').record,
            container = me.getContainer(),
            router = me.getController('Uni.controller.history.Router');

        form.setLoading();
        record.retry(encodeURIComponent(router.arguments.deviceId), {
            isNotEdit: true,
            jsonData: _.pick(container.device.getData(), 'version'),
            success: function () {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceFirmware.upgrade.retried', 'FWC', 'Firmware upload retried'));
                router.getRoute().forward();
            },
            callback: function () {
                form.setLoading(false);
            }
        });
    },

    doCancelUpgrade: function (btn) {
        var me = this,
            form = btn.up('form'),
            record = form.down('#message-pending').record,
            container = me.getContainer(),
            router = me.getController('Uni.controller.history.Router'),
            Model = Ext.ModelManager.getModel('Fwc.devicefirmware.model.FirmwareMessage'),
            devicemessageId = record.get('firmwareDeviceMessageId'),
            message = new Model();

        form.setLoading();
        message.getProxy().setExtraParam('deviceId', Uni.util.Common.decodeURIArguments(router.arguments.deviceId));
        message.setId(devicemessageId);
        message.set('version', container.device.get('version'));
        message.destroy({
            isNotEdit: true,
            success: function () {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceFirmware.upgrade.cancelled', 'FWC', 'Firmware upload cancelled'));
                router.getRoute().forward();
            },
            failure: function () {
                message.reject();
            },
            callback: function () {
                form.setLoading(false);
            }
        });
    },

    loadDevice: function (deviceId, callback) {
        var me = this,
            model = Ext.ModelManager.getModel('Mdc.model.Device'),
            container = me.getContainer();

        container.setLoading();
        model.load(deviceId, {
            success: function (device) {
                me.getApplication().fireEvent('loadDevice', device);
                container.device = device;
                if (callback) {
                    callback(device);
                }
            },
            callback: function () {
                container.setLoading(false);
            }
        });
    },

    showDeviceFirmware: function (deviceId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            storeDevHistory = me.getStore('Fwc.devicefirmware.store.DeviceFirmwareHistoryStore'),
            store = me.getStore('Fwc.devicefirmware.store.Firmwares'),
            actionsStore = me.getStore('Fwc.devicefirmware.store.FirmwareActions'),
            widget;

        storeDevHistory.getProxy().setUrl(deviceId);
        me.loadDevice(deviceId, function (device) {
            me.getApplication().fireEvent('loadDevice', device);
            me.getApplication().fireEvent('changecontentevent', 'device-firmware-setup', {
                router: router,
                device: device
            });

            widget = me.getSetupPage();
            widget.setLoading();
            store.getProxy().setExtraParam('deviceId', device.get('name'));
            actionsStore.getProxy().setExtraParam('deviceId', device.get('name'));

            var container = widget.down('#device-firmwares');
            store.load({
                callback: function (records, operation, success) {
                    if (success) {
                        Ext.suspendLayouts();
                        container.removeAll();
                        container.add(records.map(function (record) {
                            return Ext.create('Fwc.devicefirmware.view.FirmwareForm', {
                                record: record,
                                router: router,
                                device: device,
                                image: record.get('type') === 'Image'
                            });
                        }));
                        actionsStore.load();
                        Ext.resumeLayouts();
                        container.doLayout();
                    }

                    widget.setLoading(false);
                }
            });
        });
    },

    showDeviceFirmwareUpload: function (deviceId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            device = Ext.ModelManager.getModel('Mdc.model.Device'),
            messageSpecModel = me.getModel('Fwc.devicefirmware.model.FirmwareMessageSpec'),
            action = router.queryParams.action,
            container = me.getContainer();

        container.setLoading();
        device.load(deviceId, {
            success: function (device) {
                me.getApplication().fireEvent('loadDevice', device);
                messageSpecModel.getProxy().setExtraParam('deviceId', deviceId);
                messageSpecModel.getProxy().setExtraParam('firmwareType', router.queryParams.firmwareType);
                messageSpecModel.load(action, {
                    success: function (record) {
                        var widget = Ext.widget('device-firmware-upload', {
                            device: device,
                            router: router,
                            title: record.get('localizedValue')
                        });

                        me.getApplication().fireEvent('changecontentevent', widget);
                        me.getApplication().fireEvent('uploadfirmwareoption', record.get('localizedValue'));
                        widget.down('property-form').loadRecord(record);
                        container.setLoading(false);
                    }
                });
            }
        });
    },

    doActivateVersion: function (btn) {
        var me = this,
            form = btn.up('form'),
            record = form.down('#message-pending').record,
            container = me.getContainer(),
            router = me.getController('Uni.controller.history.Router'),
            Model = Ext.ModelManager.getModel('Fwc.devicefirmware.model.FirmwareMessage'),
            devicemessageId = record.get('firmwareDeviceMessageId'),
            message = new Model(),
            confirmationMessage = Ext.widget('confirm-activate-version-window', {
                itemId: 'activate-version-confirm-window',
                versionName: record.get('firmwareVersion'),
                activateHandler: function () {
                    var releaseDate = confirmationMessage.down('upload-field-container').getValue();

                    form.setLoading();
                    message.getProxy().setExtraParam('deviceId', router.arguments.deviceId);
                    message.beginEdit();
                    message.setId(devicemessageId);
                    message.set('releaseDate', releaseDate ? releaseDate.getTime() : new Date().getTime());
                    message.set('version', container.device.get('version'));
                    message.endEdit();
                    Ext.Ajax.request({
                        url: "/api/fwc/devices/" + router.arguments.deviceId + '/firmwaremessages/' + devicemessageId + '/activate',
                        isNotEdit: true,
                        method: 'PUT',
                        jsonData: Ext.encode(message.getData()),
                        success: function () {
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceFirmware.activation.success', 'FWC', 'Firmware activation has started'));
                            router.getRoute().forward();
                        },
                        failure: function () {
                            message.reject();
                        },
                        callback: function () {
                            form.setLoading(false);
                        }
                    });
                    confirmationMessage.close();
                }
            });

        confirmationMessage.show();
    },
    showDeviceFirmwareHistory: function (deviceId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            store = me.getStore('Fwc.devicefirmware.store.DeviceFirmwareHistoryStore'),
            model = me.getModel('Fwc.devicefirmware.model.DeviceFirmwareHistoryModel'),
            widget;
        store.getProxy().setUrl(deviceId);
        model.load(deviceId, {
            success: function (record) {
                view = Ext.widget('device-firmware-setup', {
                    router: router,
                    record: record
                });
                me.getApplication().fireEvent('changecontentevent', view);
            }
        });
    },
    chooseAction: function (menu, item) {
        var me = this,
            record = menu.record;

        switch (item.action) {
            case 'viewLog':
                me.getController('Uni.controller.history.Router')
                    .getRoute('devices/device/communicationtasks/history')
                    .forward({deviceId: record.get('id'), comTaskId: record.get('firmwareTaskId')});
                break;

        }
    }
});
