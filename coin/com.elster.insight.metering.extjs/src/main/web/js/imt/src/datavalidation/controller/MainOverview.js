/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.datavalidation.controller.MainOverview', {
    extend: 'Ext.app.Controller',

    requires: [],

    views: [
        'Imt.datavalidation.view.MainOverview'
    ],

    init: function () {
    },

    showOverview: function () {
        var widget = Ext.widget('data-validation-main-overview');
        this.getApplication().fireEvent('changecontentevent', widget);
    }
});
