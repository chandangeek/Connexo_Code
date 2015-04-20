Ext.define('Fwc.devicefirmware.controller.DeviceFirmware', {
    extend: 'Ext.app.Controller',

    views: [
        'Fwc.devicefirmware.view.Setup',
        'Fwc.devicefirmware.view.Upload',
        'Fwc.devicefirmware.view.DeviceSideMenu',
        'Fwc.devicefirmware.view.FirmwareForm'
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
                    this.getController('Uni.controller.history.Router')
                        .getRoute('devices/device/firmware/upload')
                        .forward(null, {action: item.action});
                }
            },
            'device-firmware-setup button[action=cancelUpgrade]': {
                click: this.doCancelUpgrade
            },
            'device-firmware-setup button[action=retry]': {
                click: this.doRetry
            },
            'device-firmware-setup button[action=viewLog]': {
                click: function () {
                    this.getController('Uni.controller.history.Router')
                        .getRoute('devices/device/firmware/log')
                        .forward();
                }
            },
            'device-firmware-setup button[action=cancel]': {
                click: this.applyFilter
            },
            '#device-firmware-upload-form button[action=uploadFirmware]': {
                click: this.uploadFirmware
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
            record;

        uploadPage.setLoading();
        errorMsg.hide();

        model.getProxy().setUrl(router.arguments.mRID);

        record = Ext.create(model, {
            uploadOption: messageSpec.get('id'),
            localizedValue: messageSpec.get('localizedValue'),
            releaseDate: timestamp
        });
        console.log(propertyForm.isValid());

        record.propertiesStore = messageSpec.properties();
        propertyForm.updateRecord(record);


        record.save({
            success: function () {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceFirmware.upgrade.success', 'FWC', 'Firmware upgrade scheduled.'));
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

                        propertyForm.getForm().markInvalid(errorsArr);
                        uploadPage.setLoading(false);
                    }
                }
            }
        });
    },

    doRetry: function (btn) {
        var form = btn.up('form'),
            record = form.getRecord(),
            router = this.getController('Uni.controller.history.Router'),
            model = Ext.ModelManager.getModel('Fwc.devicefirmware.model.FirmwareMessage'),
            comTaskId = 1; // todo: replace on assoceiated data

        form.setLoading();
        record.retry(comTaskId, {
            success: function (devicemessage) {
                devicemessage.destroy();
            },
            callback: function () {
                form.setLoading(false);
            }
        });
    },

    doCancelUpgrade: function (btn) {
        var form = btn.up('form'),
            record = form.getRecord(),
            router = this.getController('Uni.controller.history.Router'),
            model = Ext.ModelManager.getModel('Fwc.devicefirmware.model.FirmwareMessage'),
            devicemessageId = 1; // todo: replace on assoceiated data

        form.setLoading();
        model.getProxy().setUrl(router.arguments.mRID);
        model.load(devicemessageId, {
            success: function (devicemessage) {
                devicemessage.destroy();
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

            store.load({
                callback: function (records, operation, success) {
                    if (success) {
                        records.map(function (record) {
                            var form = Ext.create('Fwc.devicefirmware.view.FirmwareForm', {record: record, router: router});
                            widget.getCenterContainer().add(form);
                        });
                        actionsStore.load();
                    }
                    widget.setLoading(false);
                }
            });
        });
    },

    showDeviceFirmwareUpload: function (mRID) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            messageSpecModel = me.getModel('Fwc.devicefirmware.model.FirmwareMessageSpec'),
            action = router.queryParams.action;

        me.loadDevice(mRID, function (device) {
            me.getApplication().fireEvent('loadDevice', device);

            messageSpecModel.getProxy().setUrl(mRID);
            messageSpecModel.load(action, {
                success: function (record) {
                    var widget = Ext.widget('device-firmware-upload', {
                        device: device,
                        router: router,
                        title: record.get('localizedValue')
                    });

                    me.getApplication().fireEvent('changecontentevent', widget);
                    widget.down('property-form').loadRecord(record);
                }
            });
        });
    }
});
