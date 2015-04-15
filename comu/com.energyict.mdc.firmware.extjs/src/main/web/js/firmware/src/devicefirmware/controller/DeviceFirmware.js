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
            messageSpecModel = me.getModel('Fwc.devicefirmware.model.FirmwareMessageSpec'),
            activateOption = router.queryParams.activate;

        messageSpecModel.getProxy().setUrl(mRID);

        var record = Ext.create(messageSpecModel, {
            id: "activateOnDate",
            displayValue: "Upload firmware with activation date"
        });

//        record.properties().add([
//            {
//                key: "FirmwareDeviceMessage.upgrade.firwareversion",
//                name: "Firmware version",
//                propertyValueInfo: { },
//                propertyTypeInfo: {
//                    simplePropertyType: "UNKNOWN",
//                    predefinedPropertyValuesInfo: {
//                        possibleValues: [
//                            {
//                                "id": 1, "name": "ASP03.01.03-12359"
//                            },
//                            {
//                                "id": 2, "name": "ASP03.01.04-4532"
//                            }
//                        ],
//                        selectionMode: "COMBOBOX",
//                        exhaustive: true
//                    }
//                },
//                required: true
//            },
//            {
//                key: "FirmwareDeviceMessage.upgrade.activationdate",
//                name: "Activation date",
//                propertyValueInfo: { },
//                propertyTypeInfo: {
//                    simplePropertyType: "CLOCK"
//                },
//                required: true
//            }
//        ]);


//        console.log(record);
//
//        switch (activateOption) {
//            case 'dont':
//                title = Uni.I18n.translate('deviceFirmware.upload', 'FWC', 'Upload firmware');
//                break;
//            case 'now':
//                title = Uni.I18n.translate('deviceFirmware.uploadActivate', 'FWC', 'Upload firmware and activate');
//                break;
//            case 'inDate':
//                title = Uni.I18n.translate('deviceFirmware.uploadActivateInDate', 'FWC', 'Upload firmware with activation date');
//                break;
//            default:
//                router.getRoute('devices/device/firmware').forward();
//                break;
//        }

        me.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                //        messageSpecModel.load(activateOption, {
//            success: function (record) {
                var widget = Ext.widget('device-firmware-upload', {device: device, title: record.get('displayValue'), router: router});
                me.getApplication().fireEvent('loadDevice', device);
                me.getApplication().fireEvent('changecontentevent', widget);
                widget.down('property-form').loadRecord(record);


                //            }
//        });
            }
        });
    }
});
