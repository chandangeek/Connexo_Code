/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.controller.MainOverview', {
    extend: 'Ext.app.Controller',

    requires: [
    ],

    views: [
        'Itk.view.MainOverview'
    ],

    init: function () {
    },

    showOverview: function () {
        var widget = Ext.widget('task-main-overview');
        this.getApplication().fireEvent('changecontentevent', widget);
    }
});
