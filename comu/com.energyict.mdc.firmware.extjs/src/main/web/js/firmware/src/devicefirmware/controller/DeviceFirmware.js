Ext.define('Fwc.devicefirmware.controller.DeviceFirmware', {
    extend: 'Ext.app.Controller',

    views: [
        'Fwc.devicefirmware.view.Setup',
        'Fwc.devicefirmware.view.Upload',
        'Fwc.devicefirmware.view.DeviceSideMenu'
    ],

    requires: [
        'Mdc.model.Device'
    ],

    stores: [
//        'Fwc.store.Firmwares',

    ],

    refs: [
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
            }
        });
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

    showDeviceFirmware: function (mRID) {
        var me = this,
            router = this.getController('Uni.controller.history.Router');

        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                me.getApplication().fireEvent('loadDevice', device);
                me.getApplication().fireEvent('changecontentevent', 'device-firmware-setup', {router: router, device: device});
            }
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
                me.getApplication().fireEvent('changecontentevent', 'device-firmware-upload', {device: device, title: title, router: router});
            }
        });




    }
});
