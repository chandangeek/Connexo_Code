Ext.define('Bpm.view.task.TaskActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.bpm-task-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'menu-edit-task',
            text: Uni.I18n.translate('bpm.menu.editTask', 'BPM', 'Edit task'),
            action: 'editTask',
            privileges: Bpm.privileges.BpmManagement.assign
        },
        {
            itemId: 'menu-perform-task',
            text: Uni.I18n.translate('bpm.menu.performTask', 'BPM', 'Perform task'),
            action: 'performTask',
            privileges: Bpm.privileges.BpmManagement.execute
        }
    ]
});


