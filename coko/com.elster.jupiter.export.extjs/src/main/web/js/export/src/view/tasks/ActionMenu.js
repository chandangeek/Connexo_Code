Ext.define('Dxp.view.tasks.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.tasks-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'edit-task',
            text: Uni.I18n.translate('general.edit', 'DXP', 'Edit'),
            action: 'editTask',
            hidden: true
        },
        {
            itemId: 'view-details',
            text: Uni.I18n.translate('general.viewDetails', 'DXP', 'View details'),
            action: 'viewDetails'
        }
    ]
});

