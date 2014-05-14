Ext.define('Mdc.view.setup.devicetype.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.device-type-logbook-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            text: 'Remove',
            action: 'remove'
        }
    ]
});
