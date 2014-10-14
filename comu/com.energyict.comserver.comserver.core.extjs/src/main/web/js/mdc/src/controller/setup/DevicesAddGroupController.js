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
        'Mdc.store.filter.DeviceTypes'
    ],

    refs: [
        {
            ref: 'devicesSearchFilterPanel',
            selector: 'devicegroup-wizard-step2 filter-top-panel'
        },
        {
            ref: 'devicesSearchSideFilterForm',
            selector: 'mdc-search-results-side-filter form'
        }
    ],

    getCriteriaPanel: function() {
        return this.getDevicesSearchFilterPanel();
    },

    getSideFilterForm: function() {
        return this.getDevicesSearchSideFilterForm();
    },

    initFilterModel: function () {
        //this.getSideFilterForm().loadRecord(Ext.create('Mdc.model.DeviceFilter'));
        var router = this.getController('Uni.controller.history.Router');
        this.getSideFilterForm().loadRecord(router.filter);
        this.setFilterView();
    }
});