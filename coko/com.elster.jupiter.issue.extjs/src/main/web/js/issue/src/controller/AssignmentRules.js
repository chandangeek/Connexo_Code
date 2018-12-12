/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.controller.AssignmentRules', {
    extend: 'Ext.app.Controller',

    requires: [
    ],

    stores: [
        'Isu.store.AssignmentRules'
    ],
    views: [
        'Isu.view.assignmentrules.Overview'
    ],

    showOverview: function () {
        var widget = Ext.widget('issue-assignment-rules-overview');
        this.getApplication().fireEvent('changecontentevent', widget);
        this.getStore('Isu.store.AssignmentRules').load();
    }
});
