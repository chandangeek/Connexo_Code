/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * Created by H251853 on 9/25/2017.
 */
Ext.define('Mdc.controller.setup.ApplyAlarmAction', {
    extend: 'Dal.controller.ApplyAction',

    init: function () {
        this.control({
            'issue-action-view issue-action-form #device-alarm-action-apply': {
                click: this.applyAction
            },
            'issues-alarms-action-menu #assign-alarm-to-me': {
                click: this.assignToMe
            },
            'issues-alarms-action-menu #unassign-alarm': {
                click: this.unassign
            }
        });
    },

    actionItemId: 'device-alarm-action-apply'

})