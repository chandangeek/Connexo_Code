Ext.define('Fwc.controller.Firmware', {
    extend: 'Ext.app.Controller',

    views: [
        'Fwc.view.firmware.FirmwareVersions',
        'Fwc.view.firmware.FirmwareOptions',
        'Fwc.view.firmware.FirmwareOptionsEdit',
        'Fwc.view.firmware.FirmwareAdd',
        'Fwc.view.firmware.FirmwareEdit',
        'Fwc.view.devicetype.SideMenu'
    ],

    requires: [
        'Mdc.model.DeviceType',
        'Fwc.model.FirmwareManagementOptions',
        'Fwc.form.OptionsHydrator',
        'Fwc.form.Hydrator'
    ],

    stores: [
        'Fwc.store.Firmwares',
        'Fwc.store.FirmwareStatuses',
        'Fwc.store.FirmwareTypes',
        'Fwc.store.SupportedFirmwareTypes'
    ],

    refs: [
        {ref: 'firmwareForm', selector: '#firmwareForm'},
        {ref: 'container', selector: 'viewport > #contentPanel'},
        {ref: 'sideFilterForm', selector: 'firmware-versions firmware-side-filter'},
        {ref: 'filterPanel', selector: 'firmware-versions filter-top-panel'},
        {ref: 'firmwareOptionsEditForm', selector: '#firmwareOptionsEditForm'}
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
                click: this.saveEditedFirmware
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
            },
            'firmware-options [action=editFirmwareOptions]': {
                click: function () {
                    this.getController('Uni.controller.history.Router')
                        .getRoute('administration/devicetypes/view/firmwareoptions/edit')
                        .forward();
                }
            },
            'firmware-options-edit [action=saveOptionsAction]': {
                click: this.saveOptionsAction
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
            data = hydrator.extract(record),
            clone = record.copy();

        data[key] = undefined;
        hydrator.hydrate(data, clone);
        clone.save();
    },

    initFilter: function () {
        var me = this,
            supportedFirmwareTypesStore = Ext.getStore('Fwc.store.SupportedFirmwareTypes'),
            onFirmwareTypesLoad = function () {
                var checked = me.getSideFilterForm().down('firmware-type').getChecked();
                if (checked.length) {
                    me.getFilterPanel().setFilter(
                        'firmwareType',
                        Uni.I18n.translate('firmware.filter.type', 'FWC', 'Type'),
                        checked.map(function (ch) {
                            return ch.boxLabel;
                        }).join(', ')
                    );
                }
            },
            router = this.getController('Uni.controller.history.Router');

        me.getSideFilterForm().loadRecord(router.filter);

        me.getFilterPanel().getContainer().removeAll();
        Ext.getStore('Fwc.store.FirmwareStatuses').on('load', function () {
            var checked = me.getSideFilterForm().down('firmware-status').getChecked();
            if (checked.length) {
                me.getFilterPanel().setFilter(
                    'firmwareStatus',
                    Uni.I18n.translate('firmware.filter.status', 'FWC', 'Status'),
                    checked.map(function (ch) {
                        return ch.boxLabel;
                    }).join(', ')
                );
            }
        }, this, {single: true});

        Ext.getStore('Fwc.store.FirmwareTypes').on('load', onFirmwareTypesLoad, this, {single: true});
        supportedFirmwareTypesStore.on('load', onFirmwareTypesLoad, this, {single: true});

        supportedFirmwareTypesStore.load({
            scope: this,
            callback: function () {
                me.getContainer().down('firmware-side-filter #side-filter-firmware-type').setVisible(supportedFirmwareTypesStore.totalCount !== 1);
            }
        });
    },

    setFinal: function (firmware) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            container = me.getContainer();

        container.setLoading();
        firmware.getProxy().setUrl(router.arguments.deviceTypeId);
        firmware.setFinal({
            success: function () {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('firmware.final.success', 'FWC', 'Firmware version set as final'));
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

        var data = firmware.getAssociatedData().firmwareType;
        Ext.create('Uni.view.window.Confirmation', {
            confirmText: Uni.I18n.translate('firmware.deprecate.button', 'FWC', 'Deprecate')
        }).show({
            msg: Uni.I18n.translate('firmware.deprecate.msg', 'FWC', 'It will not be possible to upload this firmware version on devices.'),
            title: Uni.I18n.translate('firmware.deprecate.title.' + data.id, 'FWC', 'Deprecate') + " '" + firmware.get('firmwareVersion') + "'?",
            //icon: 'icon-question',
            fn: function (btn) {
                if (btn === 'confirm') {
                    container.setLoading();
                    firmware.getProxy().setUrl(router.arguments.deviceTypeId);
                    firmware.deprecate({
                        success: function () {
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('firmware.deprecate.success', 'FWC', 'Firmware version deprecated'));
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
                supportedFirmwareTypesStore = Ext.getStore('Fwc.store.SupportedFirmwareTypes'),
                record = new firmwareStore.model;

            record.getProxy().setUrl(deviceType.getId());
            firmwareStore.getProxy().setUrl(deviceType.getId());
            supportedFirmwareTypesStore.getProxy().setUrl(deviceType.getId());
            Ext.getStore('Fwc.store.FirmwareStatuses').addFilter(function (rec) {
                return ['test', 'final'].indexOf(rec.getId()) >= 0;
            }, false);

            me.getApplication().fireEvent('changecontentevent', 'firmware-add', {
                deviceType: deviceType,
                record: record
            });

            supportedFirmwareTypesStore.load({
                scope: this,
                callback: function () {
                    me.getContainer().down('firmware-form-add #disp-firmware-type').setVisible(supportedFirmwareTypesStore.totalCount===1);
                    me.getContainer().down('firmware-form-add #radio-firmware-type').setVisible(supportedFirmwareTypesStore.totalCount!==1);
                    if (supportedFirmwareTypesStore.totalCount===1) {
                        var id = me.getContainer().down('firmware-form-add #radio-firmware-type').getStore().getAt(0).data.id;
                        var onlyType = me.getContainer().down('firmware-form-add #radio-firmware-type').getStore().getAt(0).data.localizedValue;
                        me.getContainer().down('firmware-form-add #disp-firmware-type').setValue(onlyType);
                        me.getContainer().down('firmware-form-add #radio-firmware-type').setValue({id: id});
                    }
                }
            });
        });
    },

    editFirmware: function (deviceTypeId, firmwareId) {
        var me = this,
            container = this.getContainer();

        me.loadDeviceType(deviceTypeId, function (deviceType) {
            var firmwareStore = Ext.getStore('Fwc.store.Firmwares');
            container.setLoading();
            Ext.getStore('Fwc.store.FirmwareStatuses').addFilter(function (rec) {
                return ['test', 'final'].indexOf(rec.getId()) >= 0;
            }, false);

            firmwareStore.model.getProxy().setUrl(deviceType.getId());
            firmwareStore.model.load(firmwareId, {
                success: function (firmware) {
                    me.getApplication().fireEvent('loadFirmware', firmware);
                    me.getApplication().fireEvent(
                        'changecontentevent',
                        'firmware-edit',
                        {deviceType: deviceType, record: firmware}
                    );
                    if (firmware.getFirmwareStatus().getId() === 'ghost') {
                        me.getFirmwareForm().down('firmware-status').setValue({id: 'final'});
                    }
                    if (firmware.getFirmwareStatus().getId() === 'final' && firmware.raw.isInUse) {
                        me.getFirmwareForm().down('uni-form-error-message').setText(Uni.I18n.translate('firmware.edit.versionInUse', 'FWC', 'This version is in use and can not be modified.'));
                        me.getFirmwareForm().down('uni-form-error-message').show();
                        me.getFirmwareForm().down('#text-firmware-version').disable();
                        me.getFirmwareForm().down('#firmware-field-file').disable();
                        me.getFirmwareForm().down('#createEditButton').disable();

                    }
                },
                callback: function () {
                    container.setLoading(false);
                }
            });
        });
    },

    saveFirmware: function () {
        var me = this,
            form = me.getFirmwareForm(),
            record;

        form.down('uni-form-error-message').hide();
        record = form.updateRecord().getRecord();
        var input = form.down('filefield').button.fileInputEl.dom,
            file = input.files[0],
            precallback = function (options, success, response) {
                if (success) {
                    // setting of hidden fields, needs to request
                    var origValueType = form.down('firmware-type').getValues().firmwareType;
                    form.down('#firmwareType').setValue(origValueType);
                    var origValueStatus = form.down('firmware-status').getValues().firmwareStatus;
                    form.down('#firmwareStatus').setValue(origValueStatus);
                    callback(options, success, response);
                } else {
                    me.setFormErrors(response, form);
                    form.setLoading(false);
                }
            },
            callback = function (options, success, response) {
                if (success) {
                    record.doSave(savecallback, form);
                } else {
                    me.setFormErrors(response, form);
                    form.setLoading(false);
                }
            },
            savecallback = function (options, success, response) {
                form.setLoading(false);
                if (success) {
                    form.router.getRoute('administration/devicetypes/view/firmwareversions').forward();

                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('firmware.edit.added.success', 'FWC', 'Firmware version added'));
                } else {
                    me.setFormErrors(response, form);
                }
            };

        if (file) {
            var reader = new FileReader();

            form.setLoading();
            reader.onload = function () {
                record.set('fileSize', file.size);
                record.doValidate(precallback);
            };

            reader.readAsArrayBuffer(file);
        } else {
            record.doValidate(precallback);
        }
    },

    saveEditedFirmware: function () {
        var me = this,
            form = me.getFirmwareForm(),
            record;
        form.down('uni-form-error-message').hide();
        record = form.updateRecord().getRecord();
        var input = form.down('firmware-field-file').button.fileInputEl.dom,
            file = input.files[0],
            precallback = function (options, success, response) {
                if (success) {
                    if (form.xtype == 'firmware-form-edit-ghost') {
                        var origValueVersion = form.down('#displayFirmwareVersion').getValue();
                        form.remove(form.down('#displayFirmwareVersion'));
                        form.add({
                            xtype: 'textfield',
                            name: 'firmwareVersion',
                            itemId: 'firmwareVersion',
                            hidden: true
                        });
                        form.down('#firmwareVersion').setValue(origValueVersion);
                        var origValueStatus = form.down('firmware-status').getValues().firmwareStatus;
                        form.down('#firmwareStatus').setValue(origValueStatus);
                    }
                    callback(options, success, response);
                } else {
                    me.setFormErrors(response, form);
                    form.setLoading(false);
                }
            },
            callback = function (options, success, response) {
                if (success) {
                    record.doSave(savecallback, form);
                } else {
                    me.setFormErrors(response, form);
                    form.setLoading(false);
                }
            },
            savecallback = function (options, success, response) {
                form.setLoading(false);
                if (success) {
                    form.router.getRoute('administration/devicetypes/view/firmwareversions').forward();

                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('firmware.edit.updated.success', 'FWC', 'Firmware version saved'));
                } else {
                    me.setFormErrors(response, form);
                }
            };

        if (file) {
            var reader = new FileReader();

            form.setLoading();
            reader.onload = function () {
                record.set('fileSize', file.size);
                record.doValidate(precallback);
            };

            reader.readAsBinaryString(file);
        } else {
            record.doValidate(precallback);
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
        var me = this,
            model = me.getModel('Fwc.model.FirmwareManagementOptions');

        me.loadDeviceType(deviceTypeId, function (deviceType) {
            var firmwareStore = Ext.getStore('Fwc.store.Firmwares');
            firmwareStore.getProxy().setUrl(deviceType.getId());
            firmwareStore.load();
            Ext.getStore('Fwc.store.SupportedFirmwareTypes').getProxy().setUrl(deviceType.getId());
            Ext.getStore('Fwc.store.FirmwareStatuses').clearFilter(true);

            me.getApplication().fireEvent('changecontentevent', 'firmware-versions', {deviceType: deviceType});
            me.getContainer().down('deviceTypeSideMenu #overviewLink').setText(deviceType.get('name'));
            me.getContainer().down('firmware-side-filter #side-filter-firmware-type').setVisible(false);

            model.getProxy().setUrl(deviceTypeId);
            model.load(1, {
                success: function (record) {
                    if (!record.data.isAllowed)
                    me.getContainer().down('uni-form-info-message[name=warning]').show();
                }
            });
            me.initFilter();
        });
    },

    showFirmwareOptions: function (deviceTypeId) {
        var me = this,
            container = this.getContainer(),
            model = me.getModel('Fwc.model.FirmwareManagementOptions');

        me.loadDeviceType(deviceTypeId, function (deviceType) {
            me.getApplication().fireEvent('changecontentevent', 'firmware-options', {deviceType: deviceType});
            container.down('deviceTypeSideMenu #overviewLink').setText(deviceType.get('name'));
            var widget = container.down('firmware-options');
            if (widget){
                widget.setLoading();
                model.getProxy().setUrl(deviceTypeId);
                model.load(1, {
                    success: function (record) {
                        var form = widget.down('form');
                        if (form) {
                            form.loadRecord(record);
                        }
                    },
                    callback: function () {
                        widget.setLoading(false);
                    }
                });
            }

        });
    },

    editFirmwareOptions: function (deviceTypeId) {
        var me = this,
            container = this.getContainer(),
            model = me.getModel('Fwc.model.FirmwareManagementOptions');

        model.getProxy().setUrl(deviceTypeId);
        me.loadDeviceType(deviceTypeId, function (deviceType) {
            me.getApplication().fireEvent('changecontentevent', 'firmware-options-edit', {deviceType: deviceType});
            var widget = container.down('firmware-options-edit');
            if (widget){}
                widget.setLoading();
                model.load(1, {
                    success: function (record) {
                        var form = widget.down('form');
                        if (form) {
                            form.loadRecord(record);
                        }
                   },
                    callback: function () {
                        widget.setLoading(false);
                    }
                });
        });
    },

    saveOptionsAction: function () {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            form = me.getFirmwareOptionsEditForm(),
            allowedOptionsError = form.down('#allowedOptionsError');

        form.updateRecord();
        allowedOptionsError.removeAll();
        form.getRecord().save({
            success: function () {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('firmware.options.save.success', 'FWC', 'Firmware management options saved'));
                router.getRoute('administration/devicetypes/view/firmwareoptions').forward();
            },
            failure: function (record, operation) {
                form.down('uni-form-error-message').show();
                var json = Ext.decode(operation.response.responseText);
                if (json && json.errors) {
                    form.getForm().markInvalid(json.errors);
                    Ext.each(json.errors, function(error) {
                        if (error.id === "allowedOptions") {
                            allowedOptionsError.add({
                                xtype: 'container',
                                html: error.msg,
                                style: {
                                    'color': '#eb5642'
                                },
                                margin: '0 0 -10 0'
                            })
                        }
                    });
                }
            },
            callback: function () {
                form.setLoading(false);
            }
        });
    }
});
