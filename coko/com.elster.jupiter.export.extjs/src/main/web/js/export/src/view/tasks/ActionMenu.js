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
            hidden: !Uni.Auth.hasAnyPrivilege(['privilege.update.dataExportTask','privilege.update.schedule.dataExportTask']),
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
            hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.dataExportTask'),
            action: 'removeTask'
        },
        {
            itemId: 'view-log',
            text: Uni.I18n.translate('general.log', 'DES', 'Log'),
            action: 'viewLog'
        }
    ]
});

