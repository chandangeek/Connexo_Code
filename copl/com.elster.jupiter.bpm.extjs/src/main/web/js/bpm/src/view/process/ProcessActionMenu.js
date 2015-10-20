Ext.define('Bpm.view.process.ProcessActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.bpm-process-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'menu-edit-process',
            text: Uni.I18n.translate('bpm.menu.editProcess', 'BPM', 'Edit process'),
            action: 'editProcess',
            privileges: Bpm.privileges.BpmManagement.administrateProcesses
        },
        {
            itemId: 'menu-activate-process',
            text: Uni.I18n.translate('bpm.menu.activateProcess', 'BPM', 'Activate'),
            action: 'activateProcess',
            privileges: Bpm.privileges.BpmManagement.administrateProcesses
        },
        {
            itemId: 'menu-deactivate-process',
            text: Uni.I18n.translate('bpm.menu.deactivateProcess', 'BPM', 'Deactivate'),
            action: 'deactivateProcess',
            privileges: Bpm.privileges.BpmManagement.administrateProcesses
        }
    ]
});


