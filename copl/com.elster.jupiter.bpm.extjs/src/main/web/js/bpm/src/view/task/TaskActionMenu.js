Ext.define('Bpm.view.task.TaskActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.bpm-task-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
         itemId: 'menu-open-task',
         text: Uni.I18n.translate('bpm.menu.openTask', 'BPM', 'Open task'),
         action: 'openTask',
         privileges: Bpm.privileges.BpmManagement.execute
         }
    ]
});


