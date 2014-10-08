Ext.define('Mdc.controller.setup.DevicesSearchController', {
    extend: 'Mdc.controller.setup.DevicesController',

    requires: [
        'Mdc.view.setup.devicesearch.SearchItems'
    ],

    models: [
        'Mdc.model.Device',
        'Mdc.model.DeviceFilter'
    ],

    stores: [
        'Mdc.store.Devices'
    ],

    refs: [
        {
            ref: 'devicesSearchFilterPanel',
            selector: 'mdc-search-items filter-top-panel'
        },
        {
            ref: 'devicesSearchSideFilterForm',
            selector: 'mdc-search-results-side-filter form'
        }


    ],

    prefix: '#mdc-search-items',

    showSearchItems: function () {
        var searchItems = Ext.create('Mdc.view.setup.devicesearch.SearchItems');
        var store = this.getStore('Mdc.store.Devices');
        this.getApplication().fireEvent('changecontentevent', searchItems);
        this.initFilter();
        store.load();
    },

    getSideFilterForm: function() {
        return this.getDevicesSearchSideFilterForm();
    },


    applyFilter: function () {
        debugger;
        var filterForm = this.getDevicesSearchSideFilterForm();
        filterForm.updateRecord();
        filterForm.getRecord().save();
    },

    clearFilter: function () {
        debugger;
        this.getDevicesSearchSideFilterForm().getRecord().getProxy().destroy();
    },

    setFilterView: function () {
        var filterForm = this.getDevicesSearchSideFilterForm();
        var filterView = this.getDevicesSearchFilterPanel();

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
    }
});