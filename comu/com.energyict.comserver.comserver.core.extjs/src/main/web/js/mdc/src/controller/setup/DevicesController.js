Ext.define('Mdc.controller.setup.DevicesController', {
    extend: 'Ext.app.Controller',

    /**
     * @cfg {String} itemId prefix for the component
     */
    prefix: '',

    init: function () {
        if (this.prefix) {
            var control = {};

            /*control[this.prefix + ' filter-top-panel'] = {
                removeFilter: this.removeTheFilter,
                clearAllFilters: this.clearFilter
            };*/
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

    initFilter: function () {
        var router = this.getController('Uni.controller.history.Router');
        this.getSideFilterForm().loadRecord(router.filter);
    },

    getSideFilterForm: function() {},

    applyFilter: function() {},

    clearFilter: function() {}//,

    /*removeTheFilter: function (key) {
        var router = this.getController('Uni.controller.history.Router');
        var record = router.filter;
        switch (key) {
            default:
                record.set(key, null);
        }
        record.save();
    }*/
});