/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.view.validationtask.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.cfg-validation-tasks-action-menu',
    initComponent: function() {
        this.items = [
            {
                itemId: 'edit-task',
                text: Uni.I18n.translate('general.edit', 'CFG', 'Edit'),
                privileges: Cfg.privileges.Validation.admin,
                action: 'editValidationTask',
                section: this.SECTION_EDIT
            },
            {
                itemId: 'remove-task',
                text: Uni.I18n.translate('general.remove', 'CFG', 'Remove'),
                privileges: Cfg.privileges.Validation.admin,
                action: 'removeTask',
                section: this.SECTION_REMOVE
            },
            {
                itemId: 'view-history',
                text: Uni.I18n.translate('validationTasks.general.viewHistory', 'CFG', 'View history'),
                action: 'viewHistory',
                section: this.SECTION_VIEW
            },
            {
                itemId: 'run-task',
                text: Uni.I18n.translate('validationTasks.general.run', 'CFG', 'Run'),
                action: 'runTask',
                hidden: true,
                section: this.SECTION_ACTION
            }
        ];
        this.callParent(arguments);
    }
});

