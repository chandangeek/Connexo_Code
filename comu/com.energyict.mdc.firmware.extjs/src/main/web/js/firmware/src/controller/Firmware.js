/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.controller.Firmware', {
    extend: 'Ext.app.Controller',

    views: [
        'Fwc.view.firmware.FirmwareVersions',
        'Fwc.view.firmware.FirmwareOptions',
        'Fwc.view.firmware.FirmwareOptionsEdit',
        'Fwc.view.firmware.FirmwareOptionsSignatureEdit',
        'Fwc.view.firmware.FirmwareOptionsSignatureAccessorClear',
        'Fwc.view.firmware.FirmwareAdd',
        'Fwc.view.firmware.FirmwareEdit',
        'Fwc.view.firmware.FirmwareVersionsOverview',
        'Mdc.view.setup.devicetype.SideMenu'
    ],

    requires: [
        'Mdc.model.DeviceType',
        'Fwc.model.FirmwareManagementOptions',
        'Fwc.form.OptionsHydrator',
        'Fwc.form.Hydrator',
        'Uni.view.window.Confirmation',
        'Fwc.model.SecurityAccessor'
    ],

    stores: [
        'Fwc.store.Firmwares',
        'Fwc.store.FirmwareStatuses',
        'Fwc.store.FirmwareTypes',
        'Fwc.store.SupportedFirmwareTypes',
        'Fwc.store.SecurityAccessors',
        'Fwc.store.MeterFirmwareDeps',
        'Fwc.store.CommunicationFirmwareDeps'
    ],

    refs: [
        {ref: 'firmwareForm', selector: '#firmwareForm'},
        {ref: 'container', selector: 'viewport > #contentPanel'},
        {ref: 'firmwareOptionsEditForm', selector: '#firmwareOptionsEditForm'},
        {ref: 'firmwareSignatureEditForm', selector: '#firmware-options-signature-edit'}
    ],

    deviceTypeId: null,
    tab2Activate: undefined,

    init: function () {
        var me = this;
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
            'firmware-options [action=editFirmwareOptions]': {
                click: function () {
                    this.getController('Uni.controller.history.Router')
                        .getRoute('administration/devicetypes/view/firmwareversions/editOptions', {deviceTypeId: me.deviceTypeId})
                        .forward();
                }
            },
            'firmware-options [action=editFirmwareOptionsSecurityAccessor]': {
                click: function () {
                    this.getController('Uni.controller.history.Router')
                        .getRoute('administration/devicetypes/view/firmwareversions/editSignature', {deviceTypeId: me.deviceTypeId})
                        .forward();
                }
            },
            'firmware-options [action=clearAccessorFirmwareSignatureOptions]': {
                click: this.clearAccessorFirmwareSignatureOptions
            },
            'firmware-options-edit [action=saveOptionsAction]': {
                click: this.saveOptionsAction
            },'firmware-options-signature-edit [action=saveOptionsAction]': {
                click: this.saveSignatureAction
            },
            'firmware-options-edit #cancelLink': {
                click: function() {
                    this.tab2Activate = 0;
                }
            },
            'firmware-edit #cancelLink': {
                click: function() {
                    this.tab2Activate = 1;
                }
            },
            'firmware-add #cancelLink': {
                click: function() {
                    this.tab2Activate = 1;
                }
            }
        });
    },

    setFinal: function (firmware) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            container = me.getContainer();

        this.tab2Activate = 1;
        container.setLoading();
        firmware.getProxy().setUrl(router.arguments.deviceTypeId);
        firmware.setFinal({
            isNotEdit: true,
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

        this.tab2Activate = 1;
        var data = firmware.getAssociatedData().firmwareType;
        Ext.create('Uni.view.window.Confirmation', {
            confirmText: Uni.I18n.translate('general.deprecate', 'FWC', 'Deprecate')
        }).show({
            msg: Uni.I18n.translate('firmware.deprecate.msg', 'FWC', 'It will not be possible to upload this firmware version on devices.'),
            title: Uni.I18n.translate('firmware.deprecate.title', 'FWC', "Deprecate {0} firmware '{1}'?",[data.id,firmware.get('firmwareVersion')]),
            fn: function (btn) {
                if (btn === 'confirm') {
                    container.setLoading();
                    firmware.getProxy().setUrl(router.arguments.deviceTypeId);
                    firmware.deprecate({
                        isNotEdit: true,
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
            var widget = me.getContainer().down('firmware-add');
            me.reconfigureMenu(deviceType, widget);

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
                    if (!deviceType.get('needsImageIdentifierForFirmware')) {
                        me.getFirmwareForm().down('#text-image-identifier') && me.getFirmwareForm().down('#text-image-identifier').hide();
                    }else{
                        me.getFirmwareForm().down('#text-image-identifier') && me.getFirmwareForm().down('#text-image-identifier').show();
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

                    var widget = me.getContainer().down('firmware-edit');
                    me.reconfigureMenu(deviceType, widget);

                    if (firmware.getFirmwareStatus().getId() === 'ghost') {
                        me.getFirmwareForm().down('firmware-status').setValue({id: 'final'});
                    }
                    if (!deviceType.get('needsImageIdentifierForFirmware')) {
                        me.getFirmwareForm().down('#text-image-identifier').hide();
                    }else{
                        me.getFirmwareForm().down('#text-image-identifier').show();
                    }
                    if (firmware.getFirmwareStatus().getId() === 'final' && firmware.raw.isInUse) {
                        me.getFirmwareForm().down('uni-form-error-message').setText(
                            Uni.I18n.translate('firmware.edit.versionInUse', 'FWC', 'This version is in use and can not be modified.')
                        );
                        me.getFirmwareForm().down('uni-form-error-message').show();
                        me.getFirmwareForm().down('#text-firmware-version').disable();
                        me.getFirmwareForm().down('#firmware-field-file').disable();
                        me.getFirmwareForm().down('#createEditButton').disable();
                    }

                    if (firmware.getFirmwareType().getId() === 'caConfigImage') {
                        me.getFirmwareForm().down('#text-image-identifier').hide();
                        me.getFirmwareForm().down('#text-firmware-version').setFieldLabel(
                            Uni.I18n.translate('general.versionImageIdentifier', 'FWC', 'Version/Image identifier'));
                    } else {
                        me.getFirmwareForm().down('#text-image-identifier').show();
                        me.getFirmwareForm().down('#text-firmware-version').setFieldLabel(
                            Uni.I18n.translate('general.version', 'FWC', 'Version'));
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

        this.tab2Activate = 1;
        form.down('uni-form-error-message').hide();
        form.getForm().clearInvalid();
        record = form.updateRecord().getRecord();
        record.setFirmwareType(
            form.down('#radio-firmware-type')
                .getStore()
                .findRecord('id', form.down('#radio-firmware-type')
                    .getValue().firmwareType)
        );
        record.setFirmwareStatus(
            form.down('#radio-firmware-status')
                .getStore()
                .findRecord('id', form.down('#radio-firmware-status')
                    .getValue().firmwareStatus)
        );
        /*record.setMeterFirmwareDependency(
                    form.down('#firmware-campaign-min-meter-version')
                        .getStore()
                        .findRecord('id', form.down('#firmware-campaign-min-meter-version')
                            .getValue())
         );
         record.setCommunicationFirmwareDependency(
                             form.down('#firmware-campaign-min-communication-version')
                                 .getStore()
                                 .findRecord('id', form.down('#firmware-campaign-min-communication-version')
                                     .getValue())
         );*/

        var minMeterVersionCbx = form.down('#firmware-campaign-min-meter-version');
        var minCommunicationVersionCbx = form.down('#firmware-campaign-min-communication-version');

        if (minMeterVersionCbx && minMeterVersionCbx.getValue()){
            record.set('meterFirmwareDependency', {'id' : minMeterVersionCbx.getValue()})
        }

        if (minCommunicationVersionCbx && minCommunicationVersionCbx.getValue()){
            record.set('communicationFirmwareDependency', {'id' : minCommunicationVersionCbx.getValue()})
        }

        var input = form.down('filefield').button.fileInputEl.dom,
            file = input.files[0],
            backUrl = form.router.getRoute('administration/devicetypes/view/firmwareversions').buildUrl(),
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
                    record.doSave(
                        {
                            backUrl: backUrl,
                            callback: me.getOnSaveOptionsCallbackFunction(form, backUrl, Uni.I18n.translate('firmware.edit.added.success', 'FWC', 'Firmware version added'))
                        },
                        form
                    );
                } else {
                    me.setFormErrors(response, form);
                    form.setLoading(false);
                }
            };

        if (file) {
            var reader = new FileReader();

            form.setLoading();
            record.set('fileSize', file.size);
            record.doValidate(precallback);
        } else {
            record.set('fileSize', null);
            record.doValidate(precallback);
        }
    },

    saveEditedFirmware: function () {
        var me = this,
            form = me.getFirmwareForm(),
            record;

        this.tab2Activate = 1;
        form.down('uni-form-error-message').hide();
        form.getForm().clearInvalid();
        record = form.updateRecord().getRecord();
        var input = form.down('firmware-field-file').button.fileInputEl.dom,
            file = input.files[0],
            backUrl = form.router.getRoute('administration/devicetypes/view/firmwareversions').buildUrl(),
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
                    record.doSave(
                        {
                            backUrl: backUrl,
                            callback: me.getOnSaveOptionsCallbackFunction(form, backUrl, Uni.I18n.translate('firmware.edit.updated.success', 'FWC', 'Firmware version saved'))
                        },
                        form
                    );
                } else {
                    me.setFormErrors(response, form);
                    form.setLoading(false);
                }
            };

        if (file) {
            var reader = new FileReader();

            form.setLoading();
            record.set('fileSize', file.size);
            record.doValidate(precallback);
        } else {
            record.set('fileSize', null);
            record.doValidate(precallback);
        }
    },

    getOnSaveOptionsCallbackFunction: function(form, backUrl, acknowledgementMessage) {
        var me = this;
        return function (options, success, response) {
            var responseObject = Ext.decode(response.responseText, true);
            form.setLoading(false);
            if (responseObject) {
                if (!Ext.isEmpty(responseObject.error)) {
                    me.getController('Uni.controller.Error').concurrentErrorHandler(options, responseObject);
                } else if (!Ext.isEmpty(responseObject.errors)) {
                    me.setFormErrors(response, form);
                }
            } else {
                window.location.href = backUrl;
                me.getApplication().fireEvent('acknowledge', acknowledgementMessage);
            }
        }
    },

    setFormErrors: function (response, form) {
        form.down('uni-form-error-message').show();
        var json = Ext.decode(response.responseText);
        if (json && json.errors) {
            var errorsToShow = [];
            Ext.each(json.errors, function (item) {
                switch (item.id) {
                    case 'firmwareFileSize':
                        item.id = 'firmwareFile';
                        errorsToShow.push(item);
                        break;
                    default:
                        errorsToShow.push(item);
                        break;
                }
            });
            form.getForm().markInvalid(errorsToShow);
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

    showFirmwareVersionsOverview: function(deviceTypeId) {
        var me = this,
            model = me.getModel('Fwc.model.FirmwareManagementOptions'),
            supportedFirmwareTypesStore = Ext.getStore('Fwc.store.SupportedFirmwareTypes'),
            firmwareStore = Ext.getStore('Fwc.store.Firmwares'),
            viewport = Ext.ComponentQuery.query('viewport')[0],
            view;

        model.getProxy().setUrl(deviceTypeId);
        viewport.setLoading(true);
        me.loadDeviceType(deviceTypeId, function (deviceType) {
            Ext.getStore('Fwc.store.SupportedFirmwareTypes').getProxy().setUrl(deviceType.getId());
            Ext.getStore('Fwc.store.FirmwareStatuses').clearFilter(true);

            me.reconfigureMenu(deviceType, me.getContainer());

            model.load(1, {
                success: function (optionsRecord) {
                    view = Ext.widget('firmware-versions-overview', {
                        router: me.getController('Uni.controller.history.Router'),
                        deviceType: deviceType,
                        deviceTypeId: deviceTypeId,
                        firmwareManagementAllowed: optionsRecord.get('isAllowed'),
                        tab2Activate: me.tab2Activate
                    });

                    me.deviceTypeId = deviceTypeId;
                    me.getApplication().fireEvent('changecontentevent', view);
                    me.tab2Activate = undefined;
                    var callbackFunc = function () {
                        me.reconfigureMenu(deviceType, view);
                        firmwareStore.getProxy().setUrl(deviceType.getId());
                        firmwareStore.load({
                            callback: function () {
                                viewport.setLoading(false);
                            }
                        });
                    };

                    var widget = view.down('firmware-options'),
                        form = widget ? widget.down('#form') : null;
                    if (form) {
                        form.loadRecord(optionsRecord);

                        supportedFirmwareTypesStore.load({
                            scope: this,
                            callback: function () {
                                if (view.down('fwc-view-firmware-versions-topfilter')) {
                                    view.down('fwc-view-firmware-versions-topfilter').showOrHideFirmwareTypeFilter(supportedFirmwareTypesStore.totalCount !== 1);
                                }
                                var signatureCheckContainer = widget ? widget.down('#security-check-container') : null;
                                if (signatureCheckContainer) {
                                    if(optionsRecord.get('validateFirmwareFileSignature')){
                                        var saModel = me.getModel('Fwc.model.SecurityAccessor');
                                        saModel.getProxy().setUrl(deviceTypeId);
                                        saModel.load(null,{
                                            success: function (record) {
                                                signatureCheckContainer.show();
                                                signatureCheckContainer.loadRecord(record);
                                                callbackFunc();
                                            }
                                        });

                                    } else {
                                        signatureCheckContainer.hide();
                                        callbackFunc();
                                    }
                                }
                            }
                        });
                    }

                }
            });
        });
    },

    reconfigureMenu: function (deviceType, view) {
        var me = this;

        Ext.suspendLayouts();
        me.getApplication().fireEvent('loadDeviceType', deviceType);
        if (view.down('deviceTypeSideMenu')) {
            view.down('deviceTypeSideMenu').setDeviceTypeTitle(deviceType.get('name'));
            view.down('deviceTypeSideMenu #conflictingMappingLink').setText(
                Uni.I18n.translate('deviceConflictingMappings.ConflictingMappingCount', 'FWC', 'Conflicting mappings ({0})', deviceType.get('deviceConflictsCount'))
            );
        }
        Ext.resumeLayouts(true);
    },

    editFirmwareOptions: function (deviceTypeId) {
        var me = this,
            container = this.getContainer(),
            model = me.getModel('Fwc.model.FirmwareManagementOptions');

        model.getProxy().setUrl(deviceTypeId);
        me.loadDeviceType(deviceTypeId, function (deviceType) {
            me.getApplication().fireEvent('changecontentevent', 'firmware-options-edit', {deviceType: deviceType});
            var widget = container.down('firmware-options-edit');
            if (widget) {
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
            }
            me.reconfigureMenu(deviceType, widget);
        });
    },

    editFirmwareOptionsSignature: function (deviceTypeId) {
        var me = this,
            securityAccessorsStore = Ext.getStore('Fwc.store.SecurityAccessors'),
            model = me.getModel('Fwc.model.FirmwareManagementOptions');
        this.tab2Activate = 0;
        model.getProxy().setUrl(deviceTypeId);
        me.loadDeviceType(deviceTypeId, function (deviceType) {
            securityAccessorsStore.getProxy().setUrl(deviceTypeId);
            var saModel = me.getModel('Fwc.model.SecurityAccessor');
            saModel.getProxy().setUrl(deviceTypeId);
            saModel.load(null,{
                success: function (record) {
                    securityAccessorsStore.load({
                        callback: function () {
                           if(record.get('id')){
                               var form = me.getFirmwareSignatureEditForm();
                               form.down('#securityAccessorCombo').setValue(record.get('id'));
                               form.down('#securityAccessorCombo').setRawValue(record.get('name'));
                           }
                        }
                    })
                }
            });
            me.getApplication().fireEvent('changecontentevent', 'firmware-options-signature-edit', {deviceType: deviceType});
        });
    },

    saveSignatureAction: function () {
        var me = this,
            form = me.getFirmwareSignatureEditForm(),
            router = me.getController('Uni.controller.history.Router');
        this.tab2Activate = 0;

        Ext.Ajax.request({
            url: '/api/fwc/devicetypes/'+ router.arguments.deviceTypeId +'/securityaccessors/addsecurityaccessor/' + form.getSecurityAccessorId(),
            isNotEdit: true,
            method: 'PUT',
            success: function () {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceFirmware.saedit.success', 'FWC', 'Security accessor has been saved'));
                router.getRoute('administration/devicetypes/view/firmwareversions').forward(router.arguments);
            }
        });
    },

    clearAccessorFirmwareSignatureOptions: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router');
        this.tab2Activate = 0;
        Ext.create('Uni.view.window.Confirmation', {
            confirmText: Uni.I18n.translate('general.clear', 'FWC', 'Clear')
        }).show({
            width: 800,
            msg: Uni.I18n.translate('firmware.clear.msg', 'FWC', 'Clear security accessor?'),
            title: Uni.I18n.translate('firmware.clear.title', 'FWC', 'The security accessor will be removed from the firmware signature check of the device type'),
            fn: function (btn) {
                if (btn === 'confirm') {
                    Ext.Ajax.request({
                        url: '/api/fwc/devicetypes/'+ router.arguments.deviceTypeId +'/securityaccessors',
                        isNotEdit: true,
                        method: 'DELETE',
                        success: function () {
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceFirmware.saedit.success', 'FWC', 'Security accessor has been saved'));
                            Ext.ComponentQuery.query('viewport')[0].setLoading(true);
                            router.getRoute().forward();
                        }
                    });
                }
            }
        });
    },

    saveOptionsAction: function () {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            form = me.getFirmwareOptionsEditForm(),
            allowedOptionsError = form.down('#allowedOptionsError'),
            backUrl = router.getRoute('administration/devicetypes/view/firmwareversions').buildUrl();

        this.tab2Activate = 0;
        form.updateRecord();
        allowedOptionsError.removeAll();
        form.getRecord().save({
            backUrl: backUrl,
            success: function () {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('firmware.specs.save.success', 'FWC', 'Firmware management specifications saved'));
                window.location.href = backUrl;
            },
            failure: function (record, operation) {
                var json = Ext.decode(operation.response.responseText);
                if (json && json.errors) {
                    form.down('uni-form-error-message').show();
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
