/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.controller.ApplyAction', {
    extend: 'Isu.controller.ApplyIssueAction',

    issueModel: 'Itk.model.Issue',
    actionUrl: '/api/itk/issues/{0}/actions',
    assignUrl: '/api/itk/issues/{0}/{1}',

    init: function () {
        var me = this;
        me.control({
            'issues-action-menu #assign-issue-to-me': {
                click: this.assignToMe
            },
            'issues-action-menu #unassign-issue': {
                click: this.unassign
            },
            'issues-action-view issue-action-form #issue-action-apply': {
                click: this.applyAction
            }
        });
    }

});
