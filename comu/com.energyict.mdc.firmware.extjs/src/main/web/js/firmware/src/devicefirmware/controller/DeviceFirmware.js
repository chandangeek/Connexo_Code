Ext.define('Fwc.devicefirmware.controller.DeviceFirmware', {
    extend: 'Ext.app.Controller',

    views: [
        'Fwc.devicefirmware.view.Setup',
        'Fwc.devicefirmware.view.Upload',
        'Fwc.devicefirmware.view.DeviceSideMenu',
        'Fwc.devicefirmware.view.FirmwareForm',
        'Fwc.devicefirmware.view.ConfirmActivateVersionWindow'
    ],

    requires: [
        'Mdc.model.Device'
    ],

    models: [
        'Mdc.model.Device',
        'Fwc.devicefirmware.model.FirmwareMessage',
        'Fwc.devicefirmware.model.FirmwareMessageSpec'
    ],

    stores: [
        'Fwc.devicefirmware.store.Firmwares',
        'Fwc.devicefirmware.store.FirmwareActions'
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
                            .forward(null, {action: item.action ,firmwareType: firmwareType});
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

        container.setLoading();
        errorMsg.hide();
        propertyForm.clearInvalid();

        model.getProxy().setUrl(encodeURIComponent(router.arguments.mRID));

        record = Ext.create(model, {
            uploadOption: messageSpec.get('id'),
            localizedValue: messageSpec.get('localizedValue'),
            releaseDate: timestamp
        });

        propertyForm.updateRecord();
        record.propertiesStore = messageSpec.properties();

        record.save({
            success: function () {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceFirmware.upgrade.success', 'FWC', 'Firmware upload scheduled.'));
                container.setLoading(false);
                router.getRoute('devices/device/firmware').forward();
            },
            failure: function (record, resp) {
                var response = resp.response;
                if (response.status == 400) {
                    var responseText = Ext.decode(response.responseText, true);
                    if (responseText && responseText.errors) {
                        errorMsg.show();
                        var errorsArr = [];
                        Ext.each(responseText.errors, function (error) {
                            var errorKeyArr = error.id.split('.');
                            errorKeyArr.shift(); // remove first item, as it is not presented in property
                            errorsArr.push({id: errorKeyArr.join('.'), msg: error.msg});
                        });

                        propertyForm.markInvalid(errorsArr);
                        uploadPage.setLoading(false);
                    }
                    container.setLoading(false);
                }
            }
        });
    },

    doRun: function (record, action) {
        var me = this,
            container = this.getContainer(),
            router = this.getController('Uni.controller.history.Router');

        container.setLoading();
        Ext.Ajax.request({
            method: 'PUT',
            url: '/api/fwc/device/{mrid}/status/{action}'
                .replace('{action}', action)
                .replace('{mrid}', encodeURIComponent(router.arguments.mRID))
                .replace('{id}', record.get('comTaskId')),
            callback: function (operation, success) {
                if (success) {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceFirmware.upgrade.' + action, 'FWC', 'The firmware version is being read. Actual firmware version information will be available as soon as the action has completed.'));
                    router.getRoute().forward();
                }

                container.setLoading(false);
            }
        });
    },

    doRetry: function (btn) {
        var me = this,
            form = btn.up('form'),
            record = form.down('#message-failed').record,
            router = this.getController('Uni.controller.history.Router');

        form.setLoading();
        record.retry(encodeURIComponent(router.arguments.mRID), function (operation, success) {
            if (success) {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceFirmware.upgrade.retried', 'FWC', 'Firmware upload retried.'));
                router.getRoute().forward();
            }
            form.setLoading(false);
        });
    },

    doCancelUpgrade: function (btn) {
        var me = this,
            form = btn.up('form'),
            record = form.down('#message-pending').record,
            router = me.getController('Uni.controller.history.Router'),
            Model = Ext.ModelManager.getModel('Fwc.devicefirmware.model.FirmwareMessage'),
            devicemessageId = record.get('firmwareDeviceMessageId'),
            message = new Model();

        form.setLoading();
        message.getProxy().setUrl(encodeURIComponent(router.arguments.mRID));
        message.setId(devicemessageId);
        message.destroy({
            success: function () {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceFirmware.upgrade.cancelled', 'FWC', 'Firmware upload cancelled.'));
                router.getRoute().forward();
            },
            callback: function () {
                form.setLoading(false);
            }
        });
    },

    loadDevice: function (deviceId, callback) {
        var me = this,
            model = Ext.ModelManager.getModel('Mdc.model.Device'),
            container = this.getContainer();

        container.setLoading();
        model.load(deviceId, {
            success: function (device) {
                me.getApplication().fireEvent('loadDevice', device);
                if (callback) {
                    callback(device);
                }
            },
            callback: function () {
                container.setLoading(false);
            }
        });
    },

    showDeviceFirmware: function (mRID) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            store = me.getStore('Fwc.devicefirmware.store.Firmwares'),
            actionsStore = me.getStore('Fwc.devicefirmware.store.FirmwareActions'),
            widget;

        me.loadDevice(mRID, function (device) {
            me.getApplication().fireEvent('loadDevice', device);
            me.getApplication().fireEvent('changecontentevent', 'device-firmware-setup', {
                router: router,
                device: device
            });

            widget = me.getSetupPage();
            widget.setLoading();
            store.getProxy().setUrl(device.get('mRID'));
            actionsStore.getProxy().setUrl(device.get('mRID'));

            var container = widget.down('#device-firmwares');
            store.load({
                callback: function (records, operation, success) {
                    if (success) {
                        Ext.suspendLayouts();
                        container.removeAll();
                        container.add(records.map(function (record) {
                            return Ext.create('Fwc.devicefirmware.view.FirmwareForm', {record: record, router: router});
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

    showDeviceFirmwareUpload: function (mRID) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            device = Ext.ModelManager.getModel('Mdc.model.Device'),
            messageSpecModel = me.getModel('Fwc.devicefirmware.model.FirmwareMessageSpec'),
            action = router.queryParams.action,
            container = me.getContainer();

        container.setLoading();
        device.load(mRID, {
            success: function (device) {
                me.getApplication().fireEvent('loadDevice', device);
                messageSpecModel.getProxy().setUrl(encodeURIComponent(mRID));
                messageSpecModel.getProxy().extraParams.firmwareType = router.queryParams.firmwareType;
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
                    message.getProxy().setUrl(encodeURIComponent(router.arguments.mRID));
                    message.setId(devicemessageId);
                    message.set('releaseDate', releaseDate ? releaseDate.getTime() : new Date().getTime());
                    Ext.Ajax.request({
                        url: message.getProxy().url + '/' + devicemessageId + '/activate',
                        method: 'PUT',
                        jsonData: Ext.encode(message.getData()),
                        success: function () {
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceFirmware.activation.success', 'FWC', 'Firmware activation has started.'));
                            router.getRoute().forward();
                        },
                        callback: function () {
                            form.setLoading(false);
                        }
                    });
                    confirmationMessage.close();
                }
            });

        confirmationMessage.show();
    }
});
