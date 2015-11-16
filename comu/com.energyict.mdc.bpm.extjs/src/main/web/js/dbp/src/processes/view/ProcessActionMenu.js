Ext.define('Dbp.processes.view.ProcessActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.dbp-process-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'menu-edit-process',
            text: Uni.I18n.translate('dbp.menu.editProcess', 'DBP', 'Edit process'),
            action: 'editProcess',
            privileges: Dbp.privileges.DeviceProcesses.administrateProcesses
        },
        {
            itemId: 'menu-activate-process',
            text: Uni.I18n.translate('dbp.menu.activateProcess', 'DBP', 'Activate'),
            action: 'activateProcess',
            privileges: Dbp.privileges.DeviceProcesses.administrateProcesses
        },
        {
            itemId: 'menu-deactivate-process',
            text: Uni.I18n.translate('dbp.menu.deactivateProcess', 'DBP', 'Deactivate'),
            action: 'deactivateProcess',
            privileges: Dbp.privileges.DeviceProcesses.administrateProcesses
        }
    ]
});


