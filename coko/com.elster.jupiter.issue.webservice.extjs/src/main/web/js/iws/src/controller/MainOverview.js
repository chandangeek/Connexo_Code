/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Iws.controller.MainOverview', {
    extend: 'Ext.app.Controller',

    requires: [
    ],

    views: [
        'Iws.view.MainOverview'
    ],

    init: function () {
    },

    showOverview: function () {
        var widget = Ext.widget('webservice-issue-main-overview');
        this.getApplication().fireEvent('changecontentevent', widget);
    }
});
