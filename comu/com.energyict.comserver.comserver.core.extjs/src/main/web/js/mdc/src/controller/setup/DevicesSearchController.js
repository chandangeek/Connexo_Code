Ext.define('Mdc.controller.setup.DevicesSearchController', {
    extend: 'Mdc.controller.setup.DevicesController',

    requires: [
        'Mdc.view.setup.devicesearch.SearchItems',
        'Uni.form.filter.FilterCombobox'
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
            selector: 'mdc-search-items filter-top-panel'
        },
        {
            ref: 'devicesSearchSideFilterForm',
            selector: 'mdc-search-results-side-filter form'
        },
        {
            ref: 'mdcSearchItems',
            selector: 'mdc-search-items'
        },
        {
            ref: 'sortToolbar',
            selector: 'mdc-search-items #sortButtonsContainer'
        }


    ],

    //prefix: '#mdc-search-items',
    prefix: 'mdc-search-items',

    showSearchItems: function () {
        var searchItems = Ext.create('Mdc.view.setup.devicesearch.SearchItems');
        var store = this.getStore('Mdc.store.Devices');
        this.getApplication().fireEvent('changecontentevent', searchItems);
        this.initFilter();
        store.load();
    },

    getCriteriaPanel: function() {
        return this.getDevicesSearchFilterPanel();
    },

    getSideFilterForm: function() {
        return this.getDevicesSearchSideFilterForm();
    },

    getSearchItems: function() {
        return this.getMdcSearchItems();
    },

    getSortingToolbar: function() {
        return this.getSortToolbar();
    }


});