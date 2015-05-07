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
            action: 'editEstimationTask',
            privileges: Est.privileges.EstimationConfiguration.update
        },
        {
            itemId: 'remove-estimation-task',
            text: Uni.I18n.translate('general.remove', 'EST', 'Remove'),
            action: 'removeEstimationTask',
            privileges: Est.privileges.EstimationConfiguration.administrate
        },
		{
            itemId: 'view-estimation-task-history',
            text: Uni.I18n.translate('estimationtasks.general.viewHistory', 'EST', 'View history'),
            action: 'viewEstimationTaskHistory'
        },
        {
            itemId: 'run-estimation-task',
            text: Uni.I18n.translate('estimationtasks.general.run', 'EST', 'Run'),
            action: 'runEstimationTask',
            hidden: true
        }

    ]
});

