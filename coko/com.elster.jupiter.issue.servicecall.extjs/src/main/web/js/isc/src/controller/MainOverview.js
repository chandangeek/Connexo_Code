/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isc.controller.MainOverview', {
    extend: 'Ext.app.Controller',

    requires: [
    ],

    views: [
        'Isc.view.MainOverview'
    ],

    init: function () {
    },

    showOverview: function () {
        var widget = Ext.widget('servicecall-issue-main-overview');
        this.getApplication().fireEvent('changecontentevent', widget);
    }
});
