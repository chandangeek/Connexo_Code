Ext.define('Dxp.view.tasks.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.dxp-tasks-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'edit-task',
            text: Uni.I18n.translate('general.edit', 'DES', 'Edit'),
            privileges: Dxp.privileges.DataExport.update,
            action: 'editExportTask'
        },
        {
            itemId: 'remove-task',
            text: Uni.I18n.translate('general.remove', 'DES', 'Remove'),
            privileges: Dxp.privileges.DataExport.admin,
            action: 'removeTask'
        },
        {
            itemId: 'view-details',
            text: Uni.I18n.translate('general.viewDetails', 'DES', 'View details'),
            action: 'viewDetails'
        },
        {
            itemId: 'view-log',
            text: Uni.I18n.translate('general.viewLog', 'DES', 'View log'),
            action: 'viewLog',
            hidden: true
        },
        {
            itemId: 'view-history',
            text: Uni.I18n.translate('general.viewHistory', 'DES', 'View history'),
            action: 'viewHistory'
        },
        {
            itemId: 'run',
            text: Uni.I18n.translate('general.run', 'DES', 'Run'),
            action: 'run',
            hidden: true
        }
    ]
});

