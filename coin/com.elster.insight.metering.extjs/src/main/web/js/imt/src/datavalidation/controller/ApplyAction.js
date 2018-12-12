/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.datavalidation.controller.ApplyAction', {
    extend: 'Isu.controller.ApplyIssueAction',

    issueModel: 'Imt.datavalidation.model.Issue',
    actionUrl: '/api/isu/issues/{0}/actions',
    assignUrl: '/api/isu/issues/{0}/{1}',

    init: function () {
        this.control({
            'issue-action-view issue-action-form #device-issue-action-apply': {
                click: this.applyAction
            },
            'usagepoint-issue-action-menu #assign-valid-issue-to-me': {
                click: this.assignToMe
            },
            'usagepoint-issue-action-menu #unassign-valid-issue': {
                click: this.unassign
            }
        });
    }
});
