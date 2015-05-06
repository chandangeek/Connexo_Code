Ext.define('Cfg.view.validationtask.ActionMenu', {
    requires: [
        'Uni.I18n',
        'Uni.Auth'
    ],
    extend: 'Ext.menu.Menu',
    alias: 'widget.tasks-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'edit-task',
            text: Uni.I18n.translate('validationTasks.general.edit', 'CFG', 'Edit'),
            privileges: Cfg.privileges.Validation.admin,
            action: 'editValidationTask'
        },
        {
            itemId: 'remove-task',
            text: Uni.I18n.translate('validationTasks.general.remove', 'CFG', 'Remove'),
            privileges: Cfg.privileges.Validation.admin,
            action: 'removeTask'
        },
        {
            itemId: 'view-details',
            text: Uni.I18n.translate('validationTasks.general.viewDetails', 'CFG', 'View details'),
            action: 'viewDetails'
        },
		{
            itemId: 'view-history',
            text: Uni.I18n.translate('validationTasks.general.viewHistory', 'CFG', 'View history'),
            action: 'viewHistory'
        },
		{
            itemId: 'run',
            text: Uni.I18n.translate('validationTasks.general.run', 'CFG', 'Run'),
            action: 'run',
            hidden: true
        }


    ]
});

