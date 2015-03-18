Ext.define('Fwc.controller.Firmware', {
    extend: 'Ext.app.Controller',

    views: [
        'Fwc.view.firmware.FirmwareVersions',
        'Fwc.view.firmware.FirmwareOptions',
        'Fwc.view.firmware.FirmwareAdd',
        'Fwc.view.firmware.FirmwareEdit',
        'Fwc.view.devicetype.SideMenu'
    ],

    requires: [
        'Mdc.model.DeviceType'
    ],

    stores: [
        'Fwc.store.Firmwares'
    ],

    refs: [
        {ref: 'firmwareEditForm', selector: 'firmware-edit form'},
        {ref: 'firmwareAddForm', selector: 'firmware-add form'},
        {ref: 'container', selector: 'viewport > #contentPanel'},
        {ref: 'sideFilterForm', selector: 'firmware-versions firmware-side-filter'},
        {ref: 'filterPanel', selector: 'firmware-versions filter-top-panel'}
    ],

    init: function () {
        this.control({
            'firmware-versions [action=addFirmware]': {
                click: function () {
                    this.getController('Uni.controller.history.Router')
                        .getRoute('administration/devicetypes/view/firmwareversions/add')
                        .forward();
                }
            },
            'firmware-versions actioncolumn': {
                editFirmware: function (firmware) {
                    this.getController('Uni.controller.history.Router')
                        .getRoute('administration/devicetypes/view/firmwareversions/edit')
                        .forward({firmwareId: firmware.getId()});
                },
                setFinal: this.setFinal,
                deprecate: this.deprecate
            },
            'firmware-edit [action=saveFirmware]': {
                click: this.saveFirmware
            },
            'firmware-add [action=saveFirmware]': {
                click: this.saveFirmware
            },
            'firmware-versions filter-top-panel': {
                removeFilter: this.clearFilterByKey,
                clearAllFilters: this.clearFilter
            },
            'firmware-versions firmware-side-filter button[action=applyfilter]': {
                click: this.applyFilter
            },
            'firmware-versions firmware-side-filter button[action=clearfilter]': {
                click: this.clearFilter
            }
        });
    },

    applyFilter: function () {
        this.getSideFilterForm().updateRecord();
        this.getSideFilterForm().getRecord().save();
    },

    clearFilter: function () {
        this.getSideFilterForm().getRecord().getProxy().destroy();
    },

    clearFilterByKey: function (key) {
        var router = this.getController('Uni.controller.history.Router'),
            record = router.filter;

        record.set(key, null);
        record.save();
    },

    initFilter: function () {
        var me = this,
            router = this.getController('Uni.controller.history.Router');
        me.getSideFilterForm().loadRecord(router.filter);

        var values = me.getSideFilterForm().getValues();
        if (router.filter.get('status')) {
            me.getFilterPanel().setFilter('status', 'status', values.status);
        }
        if (router.filter.get('type')) {
            me.getFilterPanel().setFilter('type', 'type', values.type);
        }
    },

    setFinal: function (firmware) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            container = me.getContainer();

        container.setLoading();
        firmware.setFinal({
            success: function () {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('firmware.final.success', 'FWC', 'The firmware have been set as final'));
                router.getRoute().forward();
            },
            callback: function () {
                container.setLoading(false);
            }
        });
    },

    deprecate: function (firmware) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            container = me.getContainer();

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('firmware.deprecate.msg', 'FWC', 'This firmware version will no longer be available.'),
            title: Uni.I18n.translate('firmware.deprecate.title', 'FWC', 'Deprecate') + " '" + firmware.get('version') + "'?",
            fn: function (btn) {
                if (btn === 'confirm') {
                    container.setLoading();

                    firmware.deprecate({
                        success: function () {
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('firmware.deprecate.success', 'FWC', 'The firmware have been deprecated'));
                            router.getRoute().forward();
                        },
                        callback: function () {
                            container.setLoading(false);
                        }
                    });
                }
            }
        });
    },

    addFirmware: function (deviceTypeId) {
        var me = this;

        me.loadDeviceType(deviceTypeId, function (deviceType) {
            var firmwareStore = Ext.getStore('Fwc.store.Firmwares'),
                record = new firmwareStore.model;
            firmwareStore.getProxy().setUrl(deviceType.getId());

            me.getApplication().fireEvent('changecontentevent', 'firmware-add', {deviceType: deviceType, record: record});
        });
    },

    editFirmware: function (deviceTypeId, firmwareId) {
        var me = this,
            container = this.getContainer();

        me.loadDeviceType(deviceTypeId, function (deviceType) {
            var firmwareStore = Ext.getStore('Fwc.store.Firmwares');
            container.setLoading();

            firmwareStore.getProxy().setUrl(deviceType.getId());
            firmwareStore.model.load(firmwareId, {
                success: function (firmware) {
                    me.getApplication().fireEvent('changecontentevent', 'firmware-edit', {deviceType: deviceType, record: firmware});
                },
                callback: function () {
                    // todo: remove this!
                    me.getApplication().fireEvent('changecontentevent', 'firmware-edit', {deviceType: deviceType, record: new firmwareStore.model});

                    container.setLoading(false);
                }
            });
        });
    },

    saveFirmware: function () {
        var me  = this,
            form = me.getFirmwareForm(),
            record;

        if (form.isValid()) {
            record = form.updateRecord().getRecord();

            var input = form.down('filefield').extractFileInput();
            var file = input.files[0];
            var reader = new FileReader();

            reader.onload = function () {
                record.set('fileSize', file.size);
                record.set('file', reader.result);

                record.doValidate({
                    success: function () {
                        record.save({
                            success: function () {
                                form.router.getRoute('administration/devicetypes/view/firmwareversions').forward();
                            },
                            callback: function () {
                                //todo: add error handler here
                            }
                        });
                    },
                    callback: function () {
                        //todo: add error handler here
                    }
                });
            };

            reader.readAsBinaryString(file);
        }
    },

    loadDeviceType: function (deviceTypeId, callback) {
        var me = this,
            model = Ext.ModelManager.getModel('Mdc.model.DeviceType'),
            container = this.getContainer();

        container.setLoading();
        model.load(deviceTypeId, {
            success: function (deviceType) {
                me.getApplication().fireEvent('loadDeviceType', deviceType);
                if (callback) {
                    callback(deviceType);
                }
            },
            callback: function () {
                container.setLoading(false);
            }
        });
    },

    showFirmwareVersions: function (deviceTypeId) {
        var me = this;

        me.loadDeviceType(deviceTypeId, function (deviceType) {
            var firmwareStore = Ext.getStore('Fwc.store.Firmwares');
            firmwareStore.getProxy().setUrl(deviceType.getId());
            firmwareStore.load();

            // todo: testing on fake data
            firmwareStore.loadRawData({
                "total": 10,
                "firmwares": [
                    {
                        "id": 1,
                        "version": "ASP03.01.03-12359",
                        "type": "meter",
                        "status": "ghost"
                    },
                    {
                        "id": 2,
                        "version": "ASP03.01.03-12359",
                        "type": "meter",
                        "status": "test"
                    },
                    {
                        "id": 3,
                        "version": "ASP03.01.03-12359",
                        "type": "meter",
                        "status": "deprecated"
                    }
                ]
            });


            me.getApplication().fireEvent('changecontentevent', 'firmware-versions', {deviceType: deviceType, store: firmwareStore});
            me.initFilter();
        });
    },

    showFirmwareOptions: function (deviceTypeId) {
        var me = this;

        me.loadDeviceType(deviceTypeId, function (deviceType) {
            me.getApplication().fireEvent('loadDeviceType', deviceType);
            me.getApplication().fireEvent('changecontentevent', 'firmware-options', {deviceType: deviceType});
        });
    }
});
