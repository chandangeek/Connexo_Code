Ext.define('Bpm.view.task.TaskActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.bpm-task-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'menu-edit-task',
            text: Uni.I18n.translate('bpm.menu.editAttributes', 'BPM', 'Edit attributes'),
            action: 'editTask',
            privileges: Bpm.privileges.BpmManagement.assign
        },
        {
            itemId: 'menu-claim-task',
            text: Uni.I18n.translate('bpm.menu.assignToMe', 'BPM', 'Assign to me'),
            action: 'assignToMeTask',
            privileges: Bpm.privileges.BpmManagement.assign
        },
        {
            itemId: 'menu-release-task',
            text: Uni.I18n.translate('bpm.menu.releaseTask', 'BPM', 'Release task'),
            action: 'releaseTask',
            privileges: Bpm.privileges.BpmManagement.assign
        },
        {
            itemId: 'menu-perform-task',
            text: Uni.I18n.translate('bpm.menu.start', 'BPM', 'Start'),
            action: 'performTask',
            privileges: Bpm.privileges.BpmManagement.execute
        },
        {
            itemId: 'menu-complete-task',
            text: Uni.I18n.translate('bpm.menu.complete', 'BPM', 'Complete'),
            action: 'completeTask',
            privileges: Bpm.privileges.BpmManagement.execute
        }
    ]
});


