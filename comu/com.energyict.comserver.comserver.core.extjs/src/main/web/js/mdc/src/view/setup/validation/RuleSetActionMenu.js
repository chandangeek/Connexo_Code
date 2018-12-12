/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.validation.RuleSetActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.validation-ruleset-actionmenu',

    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('general.view', 'MDC', 'View'),
                itemId: 'viewRuleSet',
                action: 'viewRuleSet',
                section: this.SECTION_VIEW
            },
            {
                text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                privileges: Cfg.privileges.Validation.deviceConfiguration,
                itemId: 'removeRuleSet',
                action: 'removeRuleSet',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});
