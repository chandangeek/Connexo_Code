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
        {ref: 'deviceregisterreportedit', selector: '#deviceregisterreportedit'}
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
            '#addEditButton[action=addRegisterDataAction]':{
                click: me.addRegisterData
            },
            '#addEditButton[action=editRegisterDataAction]':{
                click: me.editRegisterData
            }
        });
    },

    addRegisterData: function() {
        var me = this;

        me.setPreLoader(me.getDeviceregisterreportedit(), Uni.I18n.translate('device.registerData.creating', 'MDC', 'Creating register data'));
        me.updateRegisterData('add');
    },

    editRegisterData: function() {
        var me = this;

        me.setPreLoader(me.getDeviceregisterreportedit(), Uni.I18n.translate('device.registerData.updating', 'MDC', 'Updating register data'));
        me.updateRegisterData('edit');
    },

    updateRegisterData: function(operation) {
        var me = this,
            form = me.getDeviceregisterreportedit().down('#registerDataEditForm');
        if (form.isValid()) {
            me.hideErrorPanel();
            me[operation + 'RegisterDataRecord'](form.getValues(), {operation : operation});
        } else {
            me.clearPreLoader();
            me.showErrorPanel();
        }
    },

    editRegisterDataRecord: function(values, cfg) {
        var me = this,
            record = me.getDeviceregisterreportedit().down('#registerDataEditForm').getRecord();
        me.updateRecord(record, values, Ext.apply({
            successMessage: Uni.I18n.translate('device.registerData.updated', 'MDC', 'Register data saved')
        }, cfg));
    },

    addRegisterDataRecord: function(values, cfg) {
        var me = this,
            registerType = me.getDeviceregisterreportedit().registerType,
            record = me.getReadingModelInstanceByType(registerType);
        me.updateRecord(record, values, Ext.apply({
            successMessage: Uni.I18n.translate('device.registerData.created', 'MDC', 'Register data saved')
        }, cfg));
    },

    removeDeviceRegisterData: function() {
        var me = this,
            grid = me.getDeviceregisterreportgrid(),
            lastSelected = grid.getView().getSelectionModel().getLastSelected();

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Ext.String.format(Uni.I18n.translate('device.registerData.delete.message', 'MDC', 'The register reading with measurment time {0} will no longer be available'), Ext.util.Format.date(new Date(lastSelected.get('timeStamp')), 'M j, Y \\a\\t G:i')),
            title: Uni.I18n.translate('device.registerData.delete.title', 'MDC', 'Remove reading') + '?',
            config: {
                readingToDelete: lastSelected,
                me: me
            },
            fn: me.removeDeviceRegisterDataRecord
        });
    },

    removeDeviceRegisterDataRecord: function(btn, text, cfg) {
        if (btn === 'confirm') {
            var me = cfg.config.me,
                readingToDelete = cfg.config.readingToDelete,
                router = me.getController('Uni.controller.history.Router'),
                type = readingToDelete.get("type"),
                dataStore = me.getStore(me.getReadingTypePrefix(type));

            readingToDelete.getProxy().extraParams = ({mRID: router.arguments.mRID, registerId: router.arguments.registerId});
            readingToDelete.destroy({
                callback: function (record, operation) {
                    if(operation.wasSuccessful()) {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('device.registerData.removed', 'MDC', 'Register data successfully removed'));
                        router.getRoute('devices/device/registers/register/data').forward();
                        dataStore.load();
                    }
                }
            });
        }
    },

    getReadingTypePrefix: function(type) {
        if(!Ext.isEmpty(type)) {
            return (type.charAt(0).toUpperCase() + type.substring(1) + 'RegisterData');
        }
        return 'RegisterData';
    },

    getReadingModelClassByType: function(type) {
        var me = this;

        return ('Mdc.model.' + me.getReadingTypePrefix(type));
    },

    getReadingModelInstanceByType: function(type) {
        var me = this,
            modelClass = me.getReadingModelClassByType(type),
            record = Ext.create(modelClass);

        record.set("type", type);

        return record;
    },

    updateRecord: function(record, values, cfg) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        if (record) {
            me.setRecordValues(record, values);
            record.getProxy().extraParams = ({mRID: router.arguments.mRID, registerId: router.arguments.registerId});
            record.save({
                success: function (record) {
                    me.getApplication().fireEvent('acknowledge', cfg.successMessage);
                    router.getRoute('devices/device/registers/register/data').forward();
                },
                callback: function() {
                    me.clearPreLoader();
                }
            });
        }
    },

    setRecordValues: function(record, values) {
        if(!Ext.isEmpty(values.value)) {
            record.set("value", values.value);
        }
        record.set("timeStamp", values.timeStamp);

        if(record.get("type") == 'billing') {
            record.set("interval", {start: values.intervalStart, end: values.intervalEnd})
        }
    },

    editDeviceRegisterConfigurationDataHistory: function() {
        var me = this,
            grid = me.getDeviceregisterreportgrid(),
            lastSelected = grid.getView().getSelectionModel().getLastSelected(),
            router = me.getController('Uni.controller.history.Router');

        router.getRoute('devices/device/registers/register/data/edit').forward({timestamp:lastSelected.getData().timeStamp});
    },

    showDeviceRegisterConfigurationDataEditView: function(mRID, registerId, timestamp) {
        var me = this,
            contentPanel = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            router = me.getController('Uni.controller.history.Router');

        contentPanel.setLoading(true);
        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                me.getApplication().fireEvent('loadDevice', device);
                var model = Ext.ModelManager.getModel('Mdc.model.Register');
                model.getProxy().setExtraParam('mRID', mRID);
                model.load(registerId, {
                    success: function (register) {
                        model = Ext.ModelManager.getModel(me.getReadingModelClassByType(register.get('type')));
                        model.getProxy().extraParams = ({mRID: mRID, registerId: registerId});
                        model.load(timestamp, {
                            success: function(reading) {
                                var type = register.get('type');
                                var widget = Ext.widget('deviceregisterreportedit-' + type, {
                                    edit: true,
                                    returnLink: router.getRoute('devices/device/registers/register/data').buildUrl({mRID: mRID, registerId: registerId}),
                                    registerType: type
                                });
                                me.getApplication().fireEvent('loadRegisterConfiguration', register);
                                widget.down('form').loadRecord(reading);
                                widget.setValues(register);
                                me.getApplication().fireEvent('changecontentevent', widget);
                                widget.down('#stepsMenu').setTitle(Ext.util.Format.date(new Date(reading.get('timeStamp')), 'M j, Y \\a\\t G:i'));
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

    showDeviceRegisterConfigurationDataAddView: function(mRID, registerId) {
        var me = this,
            contentPanel = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            router = me.getController('Uni.controller.history.Router');

        contentPanel.setLoading(true);
        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                me.getApplication().fireEvent('loadDevice', device);
                var model = Ext.ModelManager.getModel('Mdc.model.Register');
                model.getProxy().setExtraParam('mRID', mRID);
                model.load(registerId, {
                    success: function (register) {
                        var type = register.get('type');
                        var widget = Ext.widget('deviceregisterreportedit-' + type, {
                            edit: false,
                            returnLink: router.getRoute('devices/device/registers/register/data').buildUrl({mRID: mRID, registerId: registerId}),
                            registerType: type
                        });
                        widget.setValues(register);
                        me.getApplication().fireEvent('loadRegisterConfiguration', register);
                        me.getApplication().fireEvent('changecontentevent', widget);
                        widget.down('#stepsMenu').setTitle(Uni.I18n.translate('device.registerData.addReading', 'MDC', 'Add reading'));
                    },

                    callback: function () {
                        contentPanel.setLoading(false);
                    }
                });
            }
        });
    },

    setPreLoader: function(target, message) {
        var me = this;
        me.preloader = Ext.create('Ext.LoadMask', {
            msg: message,
            target: target
        });
        me.preloader.show();
    },

    clearPreLoader: function() {
        var me = this;
        if(!Ext.isEmpty(me.preloader)) {
            me.preloader.destroy();
            me.preloader = null;
        }
    },

    showErrorPanel: function() {
        var me = this,
            formErrorsPlaceHolder = me.getDeviceregisterreportedit().down('#registerDataEditForm #registerDataEditFormErrors');

        formErrorsPlaceHolder.hide();
        formErrorsPlaceHolder.removeAll();
        formErrorsPlaceHolder.add({
            html: Uni.I18n.translate('device.registerData.form.errors', 'MDC', 'There are errors on this page that require your attention')
        });
        formErrorsPlaceHolder.show();
    },

    hideErrorPanel: function() {
        var me = this,
            formErrorsPlaceHolder = me.getDeviceregisterreportedit().down('#registerDataEditForm #registerDataEditFormErrors');

        formErrorsPlaceHolder.hide();
        formErrorsPlaceHolder.removeAll();
    }
});

