Ext.define('Bpm.view.task.TaskActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.bpm-task-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'menu-view-log',
            text: Uni.I18n.translate('bpm.menu.TBD', 'BPM', 'TBD'),
            action: 'viewLog'
        }
    ]
});


