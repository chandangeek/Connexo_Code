/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.view.validation.RuleActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.validation-rule-action-menu',
    itemId: 'rule-action-menu',
    initComponent: function() {
        this.items = [
            {
                itemId: 'activate',
                text: Uni.I18n.translate('validation.activate', 'CFG', 'Activate'),
                privileges: Cfg.privileges.Validation.admin,
                action: 'activateRule',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'deactivate',
                text: Uni.I18n.translate('validation.deactivate', 'CFG', 'Deactivate'),
                privileges: Cfg.privileges.Validation.admin,
                action: 'deactivateRule',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'editRule',
                text: Uni.I18n.translate('general.edit', 'CFG', 'Edit'),
                privileges: Cfg.privileges.Validation.admin,
                action: 'editRule',
                section: this.SECTION_EDIT
            },
            {
                itemId: 'deleteRule',
                text: Uni.I18n.translate('general.remove', 'CFG', 'Remove'),
                privileges: Cfg.privileges.Validation.admin,
                action: 'deleteRule',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});

