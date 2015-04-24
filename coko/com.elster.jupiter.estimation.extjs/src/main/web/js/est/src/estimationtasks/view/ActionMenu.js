Ext.define('Est.estimationtasks.view.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.estimationtasks-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'edit-estimation-task',
            text: Uni.I18n.translate('general.edit', 'EST', 'Edit'),
//            hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.estimationConfiguration'),
            action: 'editEstimationTask'
        },
        {
            itemId: 'remove-estimation-task',
            text: Uni.I18n.translate('general.remove', 'EST', 'Remove'),
//            hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.estimationConfiguration'),
            action: 'removeEstimationTask'
        },
		{
            itemId: 'view-estimation-task-history',
            text: Uni.I18n.translate('estimationtasks.general.viewHistory', 'EST', 'View history'),
            action: 'viewEstimationTaskHistory'
        },
        {
            itemId: 'run-estimation-task',
            text: Uni.I18n.translate('estimationtasks.general.run', 'EST', 'Run'),
            action: 'runEstimationTask'
        }

    ]
});

