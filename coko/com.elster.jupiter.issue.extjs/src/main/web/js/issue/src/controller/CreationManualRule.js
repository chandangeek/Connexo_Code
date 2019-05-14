/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.controller.CreationManualRule', {
    extend: 'Ext.app.Controller',

    stores: [
        'Isu.store.IssueDevices',
        'Isu.store.IssueTypes',
        'Isu.store.DueinTypes',
        'Isu.store.IssueWorkgroupAssignees',
        'Isu.store.UserList'
    ],

    views: [
        'Isu.view.issues.ManuallyRuleItem'
    ],

    refs: [],

    init: function () {
        this.control({
            'issue-manually-creation-rules-item button[action=saveIssueAction]': {
                click: this.saveAction
            }
        });
    },

    createNewManuallyIssue: function () {
        var widget = Ext.widget('issue-manually-creation-rules-item');
        this.getApplication().fireEvent('changecontentevent', widget);

    },

    saveAction: function (){
    }
});