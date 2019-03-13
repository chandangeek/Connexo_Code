/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.view.task.TaskActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.bpm-task-action-menu',
    initComponent: function() {

        this.items = [
            {
                itemId: 'menu-perform-task',
                text: Uni.I18n.translate('bpm.menu.start', 'BPM', 'Start'),
                action: 'performTask',
                privileges: Bpm.privileges.BpmManagement.execute,
                section: this.SECTION_ACTION
            },
            {
                itemId: 'menu-complete-task',
                text: Uni.I18n.translate('bpm.menu.complete', 'BPM', 'Complete'),
                action: 'completeTask',
                privileges: Bpm.privileges.BpmManagement.execute,
                section: this.SECTION_ACTION
            },
            {
                itemId: 'menu-claim-task',
                text: Uni.I18n.translate('bpm.menu.assignToMe', 'BPM', 'Assign to me'),
                action: 'assignToMeTask',
                privileges: Bpm.privileges.BpmManagement.assign,
                section: this.SECTION_ACTION,
                visible: false
            },
            {
                itemId: 'menu-unassigned-task',
                text: Uni.I18n.translate('bpm.menu.releaseTask', 'BPM', 'Unassign'),
                action: 'unassignedTask',
                privileges: Bpm.privileges.BpmManagement.assign,
                section: this.SECTION_ACTION,
                visible: false
            },
            {
                itemId: 'menu-edit-task',
                text: Uni.I18n.translate('bpm.menu.editAttributes', 'BPM', 'Edit attributes'),
                action: 'editTask',
                privileges: Bpm.privileges.BpmManagement.assign,
                section: this.SECTION_EDIT
            }
        ];
        this.callParent(arguments);
    }
});


