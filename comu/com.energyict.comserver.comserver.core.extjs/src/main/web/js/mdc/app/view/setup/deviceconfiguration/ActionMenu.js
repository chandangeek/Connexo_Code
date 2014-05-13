Ext.define('Mdc.view.setup.deviceconfiguration.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.device-logbook-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            text: 'Edit',
            action: 'edit'
        },
        {
            text: 'Delete',
            action: 'delete'
        }
    ]
});
