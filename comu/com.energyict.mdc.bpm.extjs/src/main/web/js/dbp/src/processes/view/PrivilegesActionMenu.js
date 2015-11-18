Ext.define('Dbp.processes.view.PrivilegesActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.dbp-privileges-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [        
        {
            itemId: 'remove-device-state',
            text: Uni.I18n.translate('general.remove', 'DBP', 'Remove'),
            privileges: Dbp.privileges.DeviceProcesses.administrateProcesses,
            action: 'removePrivileges'
        }
    ]
});