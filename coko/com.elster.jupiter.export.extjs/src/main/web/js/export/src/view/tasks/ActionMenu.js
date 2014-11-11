Ext.define('Dxp.view.tasks.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.tasks-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'edit-task',
            text: Uni.I18n.translate('general.edit', 'DES', 'Edit'),
            action: 'editExportTask'
        },
        {
            itemId: 'view-details',
            text: Uni.I18n.translate('general.viewDetails', 'DES', 'View details'),
            action: 'viewDetails'
        },
        {
            itemId: 'remove-task',
            text: Uni.I18n.translate('general.remove', 'DES', 'Remove'),
            action: 'removeTask'
        }
    ]
});

