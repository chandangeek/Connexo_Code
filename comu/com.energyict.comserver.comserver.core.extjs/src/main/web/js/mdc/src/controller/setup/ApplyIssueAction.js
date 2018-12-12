/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * Created by H251853 on 9/25/2017.
 */
Ext.define('Mdc.controller.setup.ApplyIssueAction', {
    extend: 'Isu.controller.ApplyIssueAction',

    init: function () {
        this.control({
            'issue-action-view issue-action-form #device-issue-action-apply': {
                click: this.applyAction
            },
            'issues-action-menu #assign-to-me': {
                click: this.assignToMe
            },
            'issues-action-menu #unassign': {
                click: this.unassign
            }
        });
    },

    actionItemId: 'device-issue-action-apply'

})