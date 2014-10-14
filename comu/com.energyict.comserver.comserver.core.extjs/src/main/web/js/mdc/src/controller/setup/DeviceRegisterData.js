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
        'FlagsRegisterData'
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
            selector: 'deviceRegisterDataPage #deviceRegisterDataFilterForm'
        },
        {
            ref: 'filterPanel',
            selector: 'deviceRegisterDataPage filter-top-panel'
        }
    ],

    init: function () {
        var me = this;

        me.control({
            '#deviceregisterreportsetup #deviceregisterreportgrid': {
                select: me.loadGridItemDetail
            },
            'deviceRegisterDataPage #deviceRegisterDataFilterApplyBtn': {
                click: this.applyFilter
            },
            'deviceRegisterDataPage #deviceRegisterDataFilterResetBtn': {
                click: this.clearFilter
            },
            'deviceRegisterDataPage #deviceregisterdatafilterpanel': {
                removeFilter: this.removeFilterItem,
                clearAllFilters: this.clearFilter
            }
        });
    },

    loadGridItemDetail: function (rowmodel, record, index) {
        var me = this,
            previewPanel = me.getDeviceregisterreportpreview(),
            form = previewPanel.down('form');
        previewPanel.setTitle(Ext.util.Format.date(record.get('timeStamp'), 'M j, Y \\a\\t G:i'));
        form.loadRecord(record);
    },

    showDeviceRegisterDataView: function (mRID, registerId) {
        var me = this,
            contentPanel = Ext.ComponentQuery.query('viewport > #contentPanel')[0];

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
                            router = me.getController('Uni.controller.history.Router'),
                            widget = Ext.widget('deviceregisterreportsetup-' + type, {mRID: mRID, registerId: registerId});

                        me.getApplication().fireEvent('loadRegisterConfiguration', register);
                        me.getApplication().fireEvent('changecontentevent', widget);
                        widget.down('#stepsMenu').setTitle(register.get('name'));
                        if (type === 'billing' || type === 'numerical') {
                            widget.down('deviceRegisterDataSideFilter').show();
                            if (Ext.isEmpty(router.filter.data.onlyNonSuspect)) {
                                viewOnlySuspects = (router.queryParams.onlySuspect === 'true');
                                router.filter.set('onlySuspect', viewOnlySuspects);
                                router.filter.set('onlyNonSuspect', false);
                                me.getPage().down('#suspect').setValue(viewOnlySuspects);
                                delete router.queryParams.onlySuspect;
                            }
                            dataStore.setFilterModel(router.filter);
                            me.getSideFilterForm().loadRecord(router.filter);
                            me.setFilterView();
                        }
                        dataStore.load();
                    },

                    callback: function () {
                        contentPanel.setLoading(false);
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
        filterView.down('#Reset').setText('Reset');
        if (suspectField.getValue()) {
            filterView.setFilter('onlySuspect', filterForm.down('#suspectContainer').getFieldLabel(), suspect);
        }
        if (nonSuspectField.getValue()) {
            filterView.setFilter('onlyNonSuspect', filterForm.down('#suspectContainer').getFieldLabel(), nonSuspect);
        }
    }
});

