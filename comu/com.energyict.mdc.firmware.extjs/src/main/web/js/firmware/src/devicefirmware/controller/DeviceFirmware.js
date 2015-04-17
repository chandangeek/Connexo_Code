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
                click: function  (menu, item) {
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
            var widget = Ext.widget('device-firmware-upload', {
                device: device,
                router: router
            });
            me.getApplication().fireEvent('loadDevice', device);
            me.getApplication().fireEvent('changecontentevent', widget);

            widget.setLoading();
            messageSpecModel.getProxy().setUrl(mRID);
            messageSpecModel.load(action, {
                success: function (record) {
                    debugger;
                    widget.down('property-form').loadRecord(record);
                    //widget.getCenterContainer().setTitle(record.get('displayValue'));
                },
                callback: function () {
                    widget.setLoading(false);
                }
            });
        });
    }
});
