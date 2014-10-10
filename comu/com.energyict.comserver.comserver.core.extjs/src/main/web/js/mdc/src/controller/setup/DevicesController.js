Ext.define('Mdc.controller.setup.DevicesController', {
    extend: 'Ext.app.Controller',

    /*requires: [
        'Uni.form.filter.FilterCombobox'
    ],*/

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
            control[this.prefix + ' side-filter-combo'] = {
                updateTopFilterPanelTagButtons: this.onFilterChange
            };
            control[this.prefix + ' button[action=applyfilter]'] = {
                click: this.applyFilter
            };
            control[this.prefix + ' button[action=clearfilter]'] = {
                click: this.clearFilter
            };
            this.control(control);
        }

        this.callParent(arguments);
    },

    onFilterChange: function (combo) {
    },


    initFilter: function () {
        var router = this.getController('Uni.controller.history.Router');
        this.getSideFilterForm().loadRecord(router.filter);
        this.setFilterView();
    },

    setFilterView: function () {},

    getSideFilterForm: function() {},

    applyFilter: function() {},

    clearFilter: function() {},

    removeTheFilter: function (key) {
        var router = this.getController('Uni.controller.history.Router');
        var record = router.filter;
        switch (key) {
            default:
                record.set(key, null);
        }
        record.save();
    }
});