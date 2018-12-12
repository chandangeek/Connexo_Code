/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Est.estimationtasks.view.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.estimationtasks-action-menu',
    initComponent: function() {
        this.items = [
            {
                itemId: 'edit-estimation-task',
                text: Uni.I18n.translate('general.edit', 'EST', 'Edit'),
                action: 'editEstimationTask',
                privileges: Est.privileges.EstimationConfiguration.updateTask,
                section: this.SECTION_EDIT
            },
            {
                itemId: 'remove-estimation-task',
                text: Uni.I18n.translate('general.remove', 'EST', 'Remove'),
                action: 'removeEstimationTask',
                privileges: Est.privileges.EstimationConfiguration.administrateTask,
                section: this.SECTION_REMOVE
            },
            {
                itemId: 'view-estimation-task-history',
                text: Uni.I18n.translate('estimationtasks.general.viewHistory', 'EST', 'View history'),
                action: 'viewEstimationTaskHistory',
                section: this.SECTION_VIEW
            },
            {
                itemId: 'run-estimation-task',
                text: Uni.I18n.translate('estimationtasks.general.run', 'EST', 'Run'),
                action: 'runEstimationTask',
                section: this.SECTION_ACTION,
                hidden: true
            }
        ];
        this.callParent(arguments);
    }
});

