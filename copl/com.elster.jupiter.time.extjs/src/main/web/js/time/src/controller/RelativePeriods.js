Ext.define('Tme.controller.RelativePeriods', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.ux.window.Notification'
    ],

    views: [
        'Tme.view.relativeperiod.RelativePeriodEdit'
    ],
    stores: [
        'RelativePeriods',
        'RelativePeriodCategories'
    ]

    //TODO
});
