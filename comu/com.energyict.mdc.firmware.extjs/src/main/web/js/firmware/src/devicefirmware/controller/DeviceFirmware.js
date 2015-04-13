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

    stores: [
        'Fwc.devicefirmware.store.Firmwares',
        'Fwc.devicefirmware.store.FirmwareActions'
    ],

    refs: [
        {ref: 'container', selector: 'viewport > #contentPanel'},
        {ref: 'setupPage', selector: 'device-firmware-setup'},
        {ref: 'uploadPage', selector: 'device-firmware-upload'}
    ],

    init: function () {
        this.control({
            'device-firmware-setup device-firmware-action-menu #uploadFirmware': {
                click: this.moveToUploadFirmware
            },
            'device-firmware-setup device-firmware-action-menu #uploadActivateNow': {
                click: this.moveToUploadActivateNow
            },
            'device-firmware-setup device-firmware-action-menu #uploadActivateInDate': {
                click: this.moveToUploadActivateInDate
            },
            'device-firmware-setup button[action=viewFirmwareUpgradeLog]': {
                click: this.viewUpgradeLog
            }
        });
    },

    viewUpgradeLog: function() {
        var router = this.getController('Uni.controller.history.Router'),
            logUrl = router.getRoute('devices/device/firmware/log').buildUrl();

        window.open(logUrl);
    },


    moveToUploadFirmware: function () {
        var router = this.getController('Uni.controller.history.Router');

        router.getRoute('devices/device/firmware/upload').forward(null, {activate: 'dont'});
    },

    moveToUploadActivateNow: function () {
        var router = this.getController('Uni.controller.history.Router');

        router.getRoute('devices/device/firmware/upload').forward(null, {activate: 'now'});
    },

    moveToUploadActivateInDate: function () {
        var router = this.getController('Uni.controller.history.Router');

        router.getRoute('devices/device/firmware/upload').forward(null, {activate: 'inDate'});
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
            actionsStore.load();
            store.load({
                callback: function (records, operation, success) {
                    if (success) {
                        records.map(function (record) {
                            var form = Ext.create('Fwc.devicefirmware.view.FirmwareForm', {record: record, router: router});
                            widget.getCenterContainer().add(form);
                        });
                    }
                    widget.setLoading(false);
                }
            });
        });
    },

    showDeviceFirmwareUpload: function (mRID) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            activateOption = router.queryParams.activate,
            title;

        switch (activateOption) {
            case 'dont':
                title = Uni.I18n.translate('deviceFirmware.upload', 'FWC', 'Upload firmware');
                break;
            case 'now':
                title = Uni.I18n.translate('deviceFirmware.uploadActivate', 'FWC', 'Upload firmware and activate');
                break;
            case 'inDate':
                title = Uni.I18n.translate('deviceFirmware.uploadActivateInDate', 'FWC', 'Upload firmware with activation date');
                break;
            default:
                router.getRoute('devices/device/firmware').forward();
                break;
        }

        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                me.getApplication().fireEvent('loadDevice', device);
                me.getApplication().fireEvent('changecontentevent', 'device-firmware-upload', {
                    device: device,
                    title: title,
                    router: router
                });
            }
        });


    }
});
