/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.view.validation.RuleSetActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.ruleset-action-menu',
    itemId: 'ruleset-action-menu',
    initComponent: function() {
        this.items = [
            {
                itemId: 'editRuleSet',
                text: Uni.I18n.translate('general.edit', 'CFG', 'Edit'),
                privileges: Cfg.privileges.Validation.admin,
                action: 'editRuleSet',
                section: this.SECTION_EDIT
            },
            {
                itemId: 'deleteRuleSet',
                text: Uni.I18n.translate('general.remove', 'CFG', 'Remove'),
                privileges: Cfg.privileges.Validation.admin,
                action: 'deleteRuleSet',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});
