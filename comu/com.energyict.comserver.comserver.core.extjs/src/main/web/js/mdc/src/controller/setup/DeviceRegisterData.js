Ext.define('Mdc.controller.setup.DeviceRegisterData', {
    extend: 'Ext.app.Controller',

    views: [
        'setup.deviceregisterdata.MainSetup',
        'setup.deviceregisterdata.MainGrid',
        'setup.deviceregisterdata.text.Setup',
        'setup.deviceregisterdata.text.Grid',
        'setup.deviceregisterdata.text.Preview',
        'setup.deviceregisterdata.numerical.Setup',
        'setup.deviceregisterdata.numerical.Grid',
        'setup.deviceregisterdata.numerical.Preview',
        'setup.deviceregisterdata.billing.Setup',
        'setup.deviceregisterdata.billing.Grid',
        'setup.deviceregisterdata.billing.Preview',
        'setup.deviceregisterdata.flags.Setup',
        'setup.deviceregisterdata.flags.Grid',
        'setup.deviceregisterdata.flags.Preview',
        'setup.deviceregisterdata.ValidationPreview',
        'Mdc.view.setup.deviceregisterdata.SideFilter'
    ],

    models: [
        'Mdc.model.RegisterDataFilter'
    ],

    stores: [
        'RegisterData',
        'NumericalRegisterData',
        'BillingRegisterData',
        'TextRegisterData',
        'FlagsRegisterData',
        'RegisterConfigsOfDevice'
    ],

    refs: [
        { ref: 'page', selector: 'deviceRegisterDataPage' },
        { ref: 'deviceregisterreportpreview', selector: '#deviceregisterreportpreview' },
        {
            ref: 'sideFilter',
            selector: 'deviceRegisterDataPage #deviceRegisterDataSideFilter'
        },
        {
            ref: 'sideFilterForm',
            selector: '#deviceRegisterDataFilterForm'
        },
        {
            ref: 'filterPanel',
            selector: 'deviceRegisterDataPage filter-top-panel'
        },
        {
            ref: 'registerFilter',
            selector: '#registerFilter'
        },
        {
            ref: 'stepsMenu',
            selector: '#stepsMenu'
        }
    ],

    init: function () {
        var me = this;

        me.control({
            '#deviceregisterreportsetup #deviceregisterreportgrid': {
                select: me.loadGridItemDetail
            },
            '#deviceRegisterDataFilterApplyBtn': {
                click: this.applyFilter
            },
            '#deviceRegisterDataFilterResetBtn': {
                click: this.clearFilter
            },
            'deviceRegisterDataPage #deviceregisterdatafilterpanel': {
                removeFilter: this.removeFilterItem,
                clearAllFilters: this.clearFilter
            }
        });
    },

    loadGridItemDetail: function (rowmodel, record) {
        var me = this,
            previewPanel = me.getDeviceregisterreportpreview(),
            form = previewPanel.down('form');
        previewPanel.setTitle(Ext.util.Format.date(new Date(record.get('timeStamp')), 'M j, Y \\a\\t G:i'));
        if (previewPanel.down('displayfield[name=deltaValue]')) {
            previewPanel.down('displayfield[name=deltaValue]').setVisible(!Ext.isEmpty(record.get('deltaValue')));
        }
        form.loadRecord(record);
    },

    showDeviceRegisterDataView: function (mRID, registerId, tabController) {
        var me = this,
            contentPanel = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            registersOfDeviceStore = me.getStore('RegisterConfigsOfDevice');

        contentPanel.setLoading(true);
        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                me.getApplication().fireEvent('loadDevice', device);
                var model = Ext.ModelManager.getModel('Mdc.model.Register');
                model.getProxy().setExtraParam('mRID', mRID);
                model.load(registerId, {
                    success: function (register) {
                        var viewOnlySuspects,
                            type = register.get('type'),
                            dataStore = me.getStore(type.charAt(0).toUpperCase() + type.substring(1) + 'RegisterData'),
                            router = me.getController('Uni.controller.history.Router');

                        var widget = Ext.widget('tabbedDeviceRegisterView', {device: device, router: me.getController('Uni.controller.history.Router')});
                        widget.down('#registerTabPanel').setTitle(register.get('name'));
                        me.getApplication().fireEvent('loadRegisterConfiguration', register);
                        var func = function () {
                            me.getApplication().fireEvent('changecontentevent', widget);
                            var dataReport = Ext.widget('deviceregisterreportsetup-' + type, {mRID: mRID, registerId: registerId});
                            me.getRegisterFilter().setTitle(register.get('name'));
                            widget.down('#register-data').add(dataReport);
                            var valueColumn = widget.down('grid').down('[dataIndex=value]');
                            valueColumn.setText(Uni.I18n.translate('device.registerData.value', 'MDC', 'Value') + ' (' + register.get('lastReading')['unitOfMeasure'] + ')');
                            if (type === 'billing' || type === 'numerical') {
                                me.getRegisterFilter().show();
                                if (Ext.isEmpty(router.filter.data.onlyNonSuspect)) {
                                    viewOnlySuspects = (router.queryParams.onlySuspect === 'true');
                                    router.filter.set('onlySuspect', viewOnlySuspects);
                                    router.filter.set('onlyNonSuspect', false);
                                    me.getRegisterFilter().down('#suspect').setValue(viewOnlySuspects);
                                    delete router.queryParams.onlySuspect;
                                }
                                dataStore.setFilterModel(router.filter);
                                me.getSideFilterForm().loadRecord(router.filter);
                                me.setFilterView();
                                var deltaValueColumn = widget.down('grid').down('[dataIndex=deltaValue]');
                                deltaValueColumn.setText(Uni.I18n.translate('device.registerData.deltaValue', 'MDC', 'Delta value') + ' (' + register.get('lastReading')['unitOfMeasure'] + ')');
                                deltaValueColumn.setVisible(register.get('isCumulative'));
                            }
                            dataStore.load();
                        };
                        if (registersOfDeviceStore.getTotalCount() === 0) {
                            registersOfDeviceStore.getProxy().url = registersOfDeviceStore.getProxy().url.replace('{mRID}', mRID);
                            registersOfDeviceStore.load(function () {
                                func();
                            });
                        } else {
                            func();
                        }
                    },
                    callback: function () {
                        contentPanel.setLoading(false);
                        tabController.showTab(1);
                    }
                });
            }
        });
    },

    applyFilter: function () {
        var filterForm = this.getSideFilterForm();
        filterForm.updateRecord();
        filterForm.getRecord().save();
    },

    clearFilter: function () {
        this.getSideFilterForm().getRecord().getProxy().destroy();
    },

    removeFilterItem: function (key) {
        var router = this.getController('Uni.controller.history.Router'),
            record = router.filter;

        if (key === 'onlySuspect' || key === 'onlyNonSuspect') {
            record.set(key, false);
        }
        record.save();
    },

    setFilterView: function () {
        var filterForm = this.getSideFilterForm(),
            filterView = this.getFilterPanel(),
            suspectField = filterForm.down('#suspect'),
            nonSuspectField = filterForm.down('#nonSuspect'),
            suspect = suspectField.boxLabel,
            nonSuspect = nonSuspectField.boxLabel;
        if (suspectField.getValue()) {
            filterView.setFilter('onlySuspect', filterForm.down('#suspectContainer').getFieldLabel(), suspect);
        }
        if (nonSuspectField.getValue()) {
            filterView.setFilter('onlyNonSuspect', filterForm.down('#suspectContainer').getFieldLabel(), nonSuspect);
        }
    }
})
;

