Ext.define('Mdc.controller.setup.DevicesController', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.form.filter.FilterCombobox',
        'Mdc.store.Devices',
        'Mdc.store.filter.DeviceTypes'
    ],

    stores: [
        'Devices',
        'DeviceTypes'
    ],

    /**
     * @cfg {String} itemId prefix for the component
     */
    prefix: '',

    init: function () {
        if (this.prefix) {
            var control = {};

            control[this.prefix + ' filter-top-panel'] = {
                removeFilter: this.removeTheFilter,
                clearAllFilters: this.clearFilter
            };
            control[this.prefix + ' uni-filter-combo'] = {
                updateTopFilterPanelTagButtons: this.onFilterChange,
                specialkey: this.applyFilter
            };
            control[this.prefix + ' textfield'] = {
                specialkey: this.applyFilter
            };
            control[this.prefix + ' button[action=applyfilter]'] = {
                click: this.applyFilter
            };
            control[this.prefix + ' button[action=clearfilter]'] = {
                click: this.clearFilter
            };
            control[this.prefix +  ' mdc-search-results-side-filter'] = {
                afterrender: this.initFilterModel
            };
            this.control(control);
        }

        this.callParent(arguments);
    },

    applyFilter: function () {
        var filterForm = this.getSideFilterForm();
        filterForm.updateRecord();
        filterForm.getRecord().save();
        var store = this.getStore('Mdc.store.Devices');
        var router = this.getController('Uni.controller.history.Router');
        router.filter = filterForm.getRecord();
        store.setFilterModel(router.filter);
        store.load();
    },

    clearFilter: function () {
        this.getSideFilterForm().getRecord().getProxy().destroy();
    },

    removeTheFilter: function (key) {
        var router = this.getController('Uni.controller.history.Router');
        var record = router.filter;
        switch (key) {
            default:
                record.set(key, null);
        }
        record.save();
    },

    onFilterChange: function (combo) {
        if (!_.isEmpty(combo.getRawValue())) {
            var filterView = this.getCriteriaPanel();
            filterView.setFilter(combo.getName(), combo.getFieldLabel(), combo.getRawValue());
        }
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
    },

    getSideFilterForm: function() {},

    getCriteriaPanel: function() {},

    initFilterModel: function() {}

});