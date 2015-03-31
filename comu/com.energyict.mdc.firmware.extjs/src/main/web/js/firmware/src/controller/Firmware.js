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
        'Mdc.model.DeviceType',
        'Fwc.form.Hydrator'
    ],

    stores: [
        'Fwc.store.Firmwares',
        'Fwc.store.FirmwareStatuses',
        'Fwc.store.FirmwareTypes'
    ],

    refs: [
        {ref: 'firmwareForm', selector: '#firmwareForm'},
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
            record = router.filter,
            hydrator = Ext.create('Fwc.form.Hydrator'),
            data = hydrator.extract(record),
            clone = record.copy();

        data[key] = undefined;
        hydrator.hydrate(data, clone);
        clone.save();
    },

    initFilter: function () {
        var me = this,
            router = this.getController('Uni.controller.history.Router');
        me.getSideFilterForm().loadRecord(router.filter);

        me.getFilterPanel().getContainer().removeAll();
        Ext.getStore('Fwc.store.FirmwareStatuses').on('load', function () {
            var checked = me.getSideFilterForm().down('firmware-status').getChecked();
            if (checked.length) {
                me.getFilterPanel().setFilter(
                    'firmwareStatus',
                    'firmware Status',
                    checked.map(function (ch) {return ch.boxLabel; }).join(', ')
                );
            }
        }, this, {single: true});

        Ext.getStore('Fwc.store.FirmwareTypes').on('load', function () {
            var checked = me.getSideFilterForm().down('firmware-type').getChecked();
            if (checked.length) {
                me.getFilterPanel().setFilter(
                    'firmwareType',
                    'firmware Type',
                    checked.map(function (ch) {return ch.boxLabel; }).join(', ')
                );
            }
        }, this, {single: true});
    },

    setFinal: function (firmware) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            container = me.getContainer();

        container.setLoading();
        firmware.getProxy().setUrl(router.arguments.deviceTypeId);
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
            title: Uni.I18n.translate('firmware.deprecate.title', 'FWC', 'Deprecate') + " '" + firmware.get('firmwareVersion') + "'?",
            confirmText: Uni.I18n.translate('firmware.deprecate.button', 'FWC', 'Deprecate'),
            fn: function (btn) {
                if (btn === 'confirm') {
                    container.setLoading();

                    firmware.getProxy().setUrl(router.arguments.deviceTypeId);
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

            record.getProxy().setUrl(deviceType.getId());
            firmwareStore.getProxy().setUrl(deviceType.getId());
            Ext.getStore('Fwc.store.FirmwareStatuses').addFilter(function (rec) {return ['test', 'final'].indexOf(rec.getId()) >= 0; }, false);

            me.getApplication().fireEvent('changecontentevent', 'firmware-add', {deviceType: deviceType, record: record});
        });
    },

    editFirmware: function (deviceTypeId, firmwareId) {
        var me = this,
            container = this.getContainer();

        me.loadDeviceType(deviceTypeId, function (deviceType) {
            var firmwareStore = Ext.getStore('Fwc.store.Firmwares');
            container.setLoading();
            Ext.getStore('Fwc.store.FirmwareStatuses').addFilter(function (rec) {return ['test', 'final'].indexOf(rec.getId()) >= 0; }, false);

            firmwareStore.model.getProxy().setUrl(deviceType.getId());
            firmwareStore.model.load(firmwareId, {
                success: function (firmware) {
                    me.getApplication().fireEvent(
                        'changecontentevent',
                        'firmware-edit',
                        {deviceType: deviceType, record: firmware}
                    );
                },
                callback: function () {
                    container.setLoading(false);
                }
            });
        });
    },

    saveFirmware: function () {
        var me  = this,
            form = me.getFirmwareForm(),
            record;

        form.down('uni-form-error-message').hide();
        if (form.isValid()) {
            record = form.updateRecord().getRecord();

            var input = form.down('filefield').extractFileInput(),
                file = input.files[0],
                callback = function (options, success, response) {
                    if (success) {
                        record.save({
                            success: function () {
                                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('firmware.save.success', 'FWC', 'The firmware have been updated'));
                                form.router.getRoute('administration/devicetypes/view/firmwareversions').forward();
                            },
                            failure: function (record, operation) {
                                me.setFormErrors(operation.response, form);
                            },
                            callback: function () {
                                form.setLoading(false);
                            }
                        });
                    } else {
                        me.setFormErrors(response, form);
                        form.setLoading(false);
                    }
                };

            if (file) {
                var reader = new FileReader();

                form.setLoading();
                reader.onload = function () {
                    record.set('fileSize', file.size);
                    record.set('firmwareFile', window.btoa(reader.result));
                    record.doValidate(callback);
                };

                reader.readAsBinaryString(file);
            } else {
                record.doValidate(callback);
            }
        } else {
            form.down('uni-form-error-message').show();
        }
    },

    setFormErrors: function (response, form) {
        form.down('uni-form-error-message').show();
        var json = Ext.decode(response.responseText);
        if (json && json.errors) {
            form.getForm().markInvalid(json.errors);
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
            Ext.getStore('Fwc.store.FirmwareStatuses').clearFilter(true);

            me.getApplication().fireEvent('changecontentevent', 'firmware-versions', {deviceType: deviceType});
            me.getContainer().down('deviceTypeSideMenu #overviewLink').setText(deviceType.get('name'));
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
