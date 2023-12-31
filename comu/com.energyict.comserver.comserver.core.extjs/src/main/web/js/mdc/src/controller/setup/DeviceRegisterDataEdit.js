/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.DeviceRegisterDataEdit', {
    extend: 'Ext.app.Controller',

    views: [
        'setup.deviceregisterdata.text.Edit',
        'setup.deviceregisterdata.numerical.Edit',
        'setup.deviceregisterdata.billing.Edit',
        'setup.deviceregisterdata.flags.Edit',
        'setup.deviceregisterdata.text.Grid',
        'setup.deviceregisterdata.numerical.Grid',
        'setup.deviceregisterdata.billing.Grid',
        'setup.deviceregisterdata.flags.Grid'
    ],

    refs: [
        {ref: 'deviceregisterreportgrid', selector: '#deviceregisterreportgrid'},
        {ref: 'deviceregisterreportedit', selector: '#deviceregisterreportedit'},
        {ref: 'registerDataEditForm', selector: '#registerDataEditForm'}
    ],

    init: function () {
        var me = this;

        me.control({
            'menu menuitem[action=editData]': {
                click: me.editDeviceRegisterConfigurationDataHistory
            },
            'menu menuitem[action=removeData]': {
                click: me.removeDeviceRegisterData
            },
            '#addEditButton[action=addRegisterDataAction]': {
                click: me.addRegisterData
            },
            '#addEditButton[action=editRegisterDataAction]': {
                click: me.editRegisterData
            }
        });
    },

    addRegisterData: function () {
        var me = this;

        me.setPreLoader(me.getDeviceregisterreportedit(), Uni.I18n.translate('device.registerData.creating', 'MDC', 'Creating register data'));
        me.updateRegisterData('add');
    },

    editRegisterData: function () {
        var me = this;

        me.setPreLoader(me.getDeviceregisterreportedit(), Uni.I18n.translate('device.registerData.updating', 'MDC', 'Updating register data'));
        me.updateRegisterData('edit');
    },

    updateRegisterData: function (operation) {
        var me = this,
            form = me.getDeviceregisterreportedit().down('#registerDataEditForm');
        if (form.isValid()) {
            me.hideErrorPanel();
            me[operation + 'RegisterDataRecord'](form.getValues(), {operation: operation});
        } else {
            me.clearPreLoader();
            me.showErrorPanel();
        }
    },

    editRegisterDataRecord: function (values, cfg) {
        var me = this,
            record = me.getDeviceregisterreportedit().down('#registerDataEditForm').getRecord();
        if(values.eventDate && !values.timeStamp){
            values.timeStamp = values.eventDate;
        }
        me.updateRecord(record, values, Ext.apply({
            successMessage: Uni.I18n.translate('device.registerData.updated', 'MDC', 'Register data saved')
        }, cfg));
    },

    addRegisterDataRecord: function (values, cfg) {
        var me = this,
        //   registerType = me.getDeviceregisterreportedit().registerType,
            registerSubType = me.getDeviceregisterreportedit().subType,
            record = me.getReadingModelInstanceByType(registerSubType);
        if(values.eventDate && !values.timeStamp){
            values.timeStamp = values.eventDate;
        }
        me.updateRecord(record, values, Ext.apply({
            successMessage: Uni.I18n.translate('device.registerData.created', 'MDC', 'Register data saved')
        }, cfg));
    },

    removeDeviceRegisterData: function () {
        var me = this,
            grid = me.getDeviceregisterreportgrid(),
            lastSelected = grid.getView().getSelectionModel().getLastSelected();

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Ext.String.format(
                Uni.I18n.translate('device.registerData.delete.message', 'MDC', 'The register reading with measurement time {0} will no longer be available'),
                Uni.DateTime.formatDateTimeShort(new Date(lastSelected.get('timeStamp')))
            ),
            title: Uni.I18n.translate('device.registerData.delete.title.question', 'MDC', 'Remove the reading?'),
            config: {
                readingToDelete: lastSelected,
                me: me
            },
            fn: me.removeDeviceRegisterDataRecord
        });
    },

    removeDeviceRegisterDataRecord: function (btn, text, cfg) {
        if (btn === 'confirm') {
            var me = cfg.config.me,
                readingToDelete = cfg.config.readingToDelete,
                router = me.getController('Uni.controller.history.Router'),
                type = readingToDelete.get("type"),
                dataStore = me.getStore(me.getReadingTypePrefix(type));

            readingToDelete.getProxy().setParams(router.arguments.deviceId, router.arguments.registerId);
            readingToDelete.destroy({
                callback: function (record, operation) {
                    if (operation.wasSuccessful()) {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('device.registerData.removed', 'MDC', 'Register data successfully removed'));
                        dataStore.load();
                    }
                }
            });
        }
    },


    getReadingTypePrefix: function (type) {
        if (!Ext.isEmpty(type)) {
            return (type.charAt(0).toUpperCase() + type.substring(1) + 'RegisterData');
        }
        return 'RegisterData';
    },

    getReadingModelClassByType: function (type) {
        var me = this;

        return ('Mdc.model.' + me.getReadingTypePrefix(type));
    },

    getReadingModelInstanceByType: function (type) {
        var me = this,
            modelClass = me.getReadingModelClassByType(type),
            record = Ext.create(modelClass);

        record.set("type", type==='billing'?'numerical':type);

        return record;
    },

    updateRecord: function (record, values, cfg) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        if (record) {
            me.setRecordValues(record, values);
            record.getProxy().setParams(decodeURIComponent(router.arguments.deviceId), router.arguments.registerId);
            record.save({
                success: function () {
                    me.getApplication().fireEvent('acknowledge', cfg.successMessage);
                    window.location = me.getRegisterDataEditForm().up('contentcontainer').returnLink
                },
                failure: function (record, resp) {
                    var response = resp.response;
                    if (response.status == 400) {
                        var responseText = Ext.decode(response.responseText, true);
                        if (responseText && !Ext.isEmpty(responseText.errors)) {
                            me.getRegisterDataEditForm().getForm().markInvalid(responseText.errors);
                            me.showErrorPanel();
                        }
                    }
                },
                callback: function () {
                    me.clearPreLoader();
                }
            });
        }
    },

    setRecordValues: function (record, values) {
        if (!Ext.isEmpty(values.value)) {
            record.data.value = values.value;
        }
        record.set("timeStamp", values.timeStamp==="null"?values.eventDate:values.timeStamp);
        record.set("eventDate", values.eventDate);
        record.get('isConfirmed') && record.set('isConfirmed', false);
        if (record.get("type") == 'billing' || record.get("type") == 'numerical') {
            record.set("interval", {start: values['interval.start'], end: values['interval.end']});
        } else {
            delete record.data.interval;
        }
        if(!values['interval.start'] && !values['interval.end']){
            delete record.data.interval;
        }
        if (record.get("type") == 'text') {
            delete record.data.deltaValue;
            delete record.data.isConfirmed;
        }
    },

    editDeviceRegisterConfigurationDataHistory: function () {
        var me = this,
            grid = me.getDeviceregisterreportgrid(),
            lastSelected = grid.getView().getSelectionModel().getLastSelected(),
            router = me.getController('Uni.controller.history.Router');
        router.getRoute('devices/device/registers/registerdata/edit').forward({registerId:registerBeingViewed.get('id'), timestamp: lastSelected.getData().timeStamp});
    },

    showDeviceRegisterConfigurationDataEditView: function (deviceId, registerId, timestamp) {
        var me = this,
            contentPanel = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            router = me.getController('Uni.controller.history.Router');

        contentPanel.setLoading(true);

        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceId, {
            success: function (device) {
                me.getApplication().fireEvent('loadDevice', device);
                var model = Ext.ModelManager.getModel('Mdc.model.Register');
                model.getProxy().setExtraParam('deviceId', deviceId);
                model.load(registerId, {
                    success: function (register) {
                        model = Ext.ModelManager.getModel(me.getReadingModelClassByType(register.get('type')));
                        model.getProxy().extraParams = ({deviceId: deviceId, registerId: registerId});
                        model.load(timestamp, {
                            success: function (reading) {
                                var type = register.get('type');
                                var subType = type;
                                if(type === 'numerical' && register.get('isBilling')){
                                    subType = 'billing';
                                } else {
                                    subType = type;
                                }

                                var widget = Ext.widget('deviceregisterreportedit-' + subType, {
                                    edit: true,
                                    returnLink: router.getRoute('devices/device/registers/registerdata').buildUrl({
                                        deviceId: encodeURIComponent(deviceId),
                                        registerId: registerId
                                    }) + (me.getController('Uni.controller.history.EventBus').getPreviousQueryString() !== null ? '?' + me.getController('Uni.controller.history.EventBus').getPreviousQueryString() : ''),
                                    registerType: type,
                                    subType: subType,
                                    router: router,
                                    hasEvent: register.get('hasEvent')
                                });
                                me.getApplication().fireEvent('loadRegisterConfiguration', register);
                                if (reading.get('calculatedValue')) {
                                    reading.set('value', reading.get('calculatedValue'));
                                } else if (reading.get('collectedValue')) {
                                    reading.set('value', reading.get('collectedValue'));
                                }
                                widget.down('form').loadRecord(reading);
                                widget.setValues(register);
                                me.getApplication().fireEvent('changecontentevent', widget);
                                widget.down('#stepsMenu').setTitle(
                                    Uni.DateTime.formatDateTimeShort(new Date(Number(timestamp)))
                                );
                                widget.down('#stepsMenu #editReading').setText(Uni.I18n.translate('device.registerData.editReading', 'MDC', 'Edit reading'));
                            },

                            callback: function () {
                                contentPanel.setLoading(false);
                            }
                        });
                    }
                });
            }
        });
    },

    showDeviceRegisterConfigurationDataAddView: function (deviceId, registerId) {
        var me = this,
            contentPanel = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            router = me.getController('Uni.controller.history.Router');

        contentPanel.setLoading(true);
        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceId, {
            success: function (device) {
                me.getApplication().fireEvent('loadDevice', device);
                var model = Ext.ModelManager.getModel('Mdc.model.Register');
                model.getProxy().setExtraParam('deviceId', deviceId);
                model.load(registerId, {
                    success: function (register) {
                        var type = register.get('type');
                        var subType = type;
                        if(type === 'numerical' && register.get('isBilling')){
                            subType = 'billing';
                        } else {
                            subType = type;
                        }
                        var widget = Ext.widget('deviceregisterreportedit-' + subType, {
                            edit: false,
                            returnLink: router.getRoute('devices/device/registers/registerdata').buildUrl({deviceId: encodeURIComponent(deviceId), registerId: registerId}),
                            registerType: type,
                            subType: subType,
                            deviceId: deviceId,
                            registerId: registerId,
                            router: router,
                            hasEvent: register.get('hasEvent')
                        });
                        widget.setValues(register);
                        me.getApplication().fireEvent('loadRegisterConfiguration', register);
                        me.getApplication().fireEvent('changecontentevent', widget);
                        widget.down('#stepsMenu #editReading').setText(Uni.I18n.translate('general.addReading', 'MDC', 'Add reading'));
                    },

                    callback: function () {
                        contentPanel.setLoading(false);
                    }
                });
            }
        });
    },

    setPreLoader: function (target, message) {
        var me = this;
        me.preloader = Ext.create('Ext.LoadMask', {
            msg: message,
            target: target
        });
        me.preloader.show();
    },

    clearPreLoader: function () {
        var me = this;
        if (!Ext.isEmpty(me.preloader)) {
            me.preloader.destroy();
            me.preloader = null;
        }
    },

    showErrorPanel: function () {
        var me = this,
            formErrorsPlaceHolder = me.getDeviceregisterreportedit().down('#registerDataEditForm #registerDataEditFormErrors');

        formErrorsPlaceHolder.hide();
        Ext.suspendLayouts();
        formErrorsPlaceHolder.removeAll();
        formErrorsPlaceHolder.add({
            html: Uni.I18n.translate('general.formErrors', 'MDC', 'There are errors on this page that require your attention.')
        });
        Ext.resumeLayouts();
        formErrorsPlaceHolder.show();
    },

    hideErrorPanel: function () {
        var me = this,
            formErrorsPlaceHolder = me.getDeviceregisterreportedit().down('#registerDataEditForm #registerDataEditFormErrors');

        formErrorsPlaceHolder.hide();
        formErrorsPlaceHolder.removeAll();
    }
});

