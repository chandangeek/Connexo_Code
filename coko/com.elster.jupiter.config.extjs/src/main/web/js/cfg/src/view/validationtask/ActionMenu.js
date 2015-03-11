Ext.define('Cfg.view.validationtask.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.tasks-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'edit-task',
            text: Uni.I18n.translate('dataValidationTasks.general.edit', 'CFG', 'Edit'),
            hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.validationConfiguration'),
            action: 'editValidationTask'
        },
        {
            itemId: 'remove-task',
            text: Uni.I18n.translate('dataValidationTasks.general.remove', 'CFG', 'Remove'),
            hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.validationConfiguration'),
            action: 'removeTask'
        },
        {
            itemId: 'view-details',
            text: Uni.I18n.translate('dataValidationTasks.general.viewDetails', 'CFG', 'View details'),
            action: 'viewDetails'
        },
		{
            itemId: 'view-history',
            text: Uni.I18n.translate('dataValidationTasks.general.viewHistory', 'CFG', 'View history'),
            action: 'viewHistory'
        }

    ]
});

