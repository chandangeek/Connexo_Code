/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dal.controller.ApplyAction', {
    extend: 'Isu.controller.ApplyIssueAction',

    issueModel: 'Dal.model.Alarm',
    actionUrl: '/api/dal/alarms/{0}/actions',
    assignUrl: '/api/dal/alarms/{0}/{1}',

    init: function () {
        var me = this;
        me.control({
            'alarms-action-menu #assign-alarm-to-me': {
                click: this.assignToMe
            },
            'alarms-action-menu #unassign-alarm': {
                click: this.unassign
            }
        });
    }

});
