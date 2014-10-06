Ext.define('Mdc.controller.setup.DevicesSearchController', {
    extend: 'Mdc.controller.setup.DevicesController',

    requires: [
        'Mdc.view.setup.devicesearch.SearchItems'
    ],

    refs: [
        {
            ref: 'devicesSearchFilterPanel',
            selector: '#mdc-search-items filter-top-panel'
        },
        {
            ref: 'devicesSearchSideFilterForm',
            selector: '#mdc-search-items filter-form'
        }
    ],

    prefix: '#mdc-search-items',

    showSearchItems: function () {
        var searchItems = Ext.create('Mdc.view.setup.devicesearch.SearchItems');
        this.getApplication().fireEvent('changecontentevent', searchItems);
        this.initFilter();
    },

    getSideFilterForm: function() {
        return this.getDevicesSearchSideFilterForm();
    },


    applyFilter: function () {
        debugger;
        this.getDevicesSearchSideFilterForm().updateRecord();
        this.getDevicesSearchSideFilterForm().getRecord().save();
    },

    clearFilter: function () {
        debugger;
        this.getDevicesSearchSideFilterForm().getRecord().getProxy().destroy();
    }
});