Ext.define('Tme.controller.RelativePeriods', {
    extend: 'Ext.app.Controller',

    requires: [
    ],

    views: [
    ],

    stores: [
        'RelativePeriods',
        'RelativePeriodCategories'
    ],

    showOverview: function () {
        // Nothing to do here, placeholder for now.
    },

    showAddRelativePeriod: function () {
        var me = this,
            view = Ext.create('Tme.view.relativeperiod.Edit');

        me.getApplication().fireEvent('changecontentevent', view);
    }
});
