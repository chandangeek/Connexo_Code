Ext.define('Dbp.processes.view.DeviceStatesActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.dbp-device-states-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [        
        {
            itemId: 'remove-device-state',
            text: Uni.I18n.translate('general.remove', 'DBP', 'Remove'),
            privileges: Dbp.privileges.DeviceProcesses.administrateProcesses,
            action: 'removeDeviceState'
        }
    ]
});