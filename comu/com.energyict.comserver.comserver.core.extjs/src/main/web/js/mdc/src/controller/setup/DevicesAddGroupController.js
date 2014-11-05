Ext.define('Mdc.controller.setup.DevicesAddGroupController', {
    extend: 'Mdc.controller.setup.DevicesController',

    prefix: 'add-devicegroup-browse',

    requires: [
        'Mdc.view.setup.devicesearch.SearchItems',
        'Uni.form.filter.FilterCombobox',
        'Mdc.view.setup.devicegroup.Step2',
        'Mdc.view.setup.devicegroup.Browse'
    ],

    models: [
        'Mdc.model.Device',
        'Mdc.model.DeviceFilter'
    ],

    stores: [
        'Mdc.store.Devices',
        'Mdc.store.filter.DeviceTypes',
        'Mdc.store.DevicesBuffered'
    ],

    refs: [
        {
            ref: 'devicesSearchFilterPanel',
            selector: 'devicegroup-wizard-step2 filter-top-panel'
        },
        {
            ref: 'devicesSearchSideFilterForm',
            selector: 'mdc-search-results-side-filter form'
        },
        {
            ref: 'dynamicRadioButton',
            selector: 'devicegroup-wizard-step1 #dynamicDeviceGroup'
        }
    ],

    getCriteriaPanel: function() {
        return this.getDevicesSearchFilterPanel();
    },

    getSideFilterForm: function() {
        return this.getDevicesSearchSideFilterForm();
    },

    initFilterModel: function () {
        var router = this.getController('Uni.controller.history.Router');
        this.getSideFilterForm().loadRecord(router.filter);
        this.setFilterView();
    },

    applyFilter: function () {
        this.initAddDeviceGroupActionController();
        var filterForm = this.getSideFilterForm();
        filterForm.updateRecord();
        filterForm.getRecord().save();
        var store;
        if (this.getDynamicRadioButton().checked) {
            store = this.getStore('Mdc.store.Devices');
        } else {
            store = this.getStore('Mdc.store.DevicesBuffered');
        }
        var router = this.getController('Uni.controller.history.Router');
        router.filter = filterForm.getRecord();
        store.setFilterModel(router.filter);
        store.load();
        this.setFilterView();
    },

    removeTheFilter: function (key) {
        this.initAddDeviceGroupActionController();
        var router = this.getController('Uni.controller.history.Router');
        var record = router.filter;
        switch (key) {
            default:
                record.set(key, null);
        }
        record.save();
        var filterForm = this.getSideFilterForm();
        filterForm.loadRecord(record);

        this.applyFilter();
    },

    clearFilter: function () {
        this.initAddDeviceGroupActionController();
        this.getCriteriaPanel().getContainer().removeAll();
        var router = this.getController('Uni.controller.history.Router');
        var record = router.filter;
        record.set('mRID', null);
        record.set('serialNumber', null);
        record.set('deviceTypes', null);
        record.set('deviceConfigurations', null);
        record.save();
        var filterForm = this.getSideFilterForm();
        filterForm.loadRecord(record);
        this.applyFilter();
    },

    initAddDeviceGroupActionController: function() {
        this.getApplication().getController('Mdc.controller.setup.AddDeviceGroupAction').disableCreateWidget();
    },

    setFilterView: function () {
        var filterForm = this.getSideFilterForm();
        var filterView = this.getCriteriaPanel();

        var serialNumberField = filterForm.down('[name=serialNumber]');
        var serialNumberValue = serialNumberField.getValue().trim();
        var mRIDField = filterForm.down('[name=mRID]');
        var mRIDValue = mRIDField.getValue().trim();

        if (serialNumberValue != "") {
            filterView.setFilter('serialNumber', serialNumberField.getFieldLabel(), serialNumberValue);
        }
        if (mRIDValue != "") {
            filterView.setFilter('mRID', mRIDField.getFieldLabel(), mRIDValue);
        }

        var deviceTypeCombo = filterForm.down('[name=deviceTypes]');
        var deviceConfigsCombo = filterForm.down('[name=deviceConfigurations]');

        if (!_.isEmpty(deviceTypeCombo.getRawValue())) {
            var filterView = this.getCriteriaPanel();
            filterView.setFilter(deviceTypeCombo.getName(), deviceTypeCombo.getFieldLabel(), deviceTypeCombo.getRawValue());
        }

        if (!_.isEmpty(deviceConfigsCombo.getRawValue())) {
            var filterView = this.getCriteriaPanel();
            filterView.setFilter(deviceConfigsCombo.getName(), deviceConfigsCombo.getFieldLabel(), deviceConfigsCombo.getRawValue());
        }
    }

});