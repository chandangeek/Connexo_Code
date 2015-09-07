Ext.define('Dxp.view.tasks.DestinationActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.dxp-tasks-destination-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'edit-destination',
            text: Uni.I18n.translate('general.edit', 'DES', 'Edit'),
            privileges: Dxp.privileges.DataExport.update,
            action: 'editDestination'
        },
        {
            itemId: 'remove-destination',
            text: Uni.I18n.translate('general.remove', 'DES', 'Remove'),
            privileges: Dxp.privileges.DataExport.admin,
            action: 'removeDestination'
        }
    ]
});

