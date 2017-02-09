/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.view.validation.RuleSetSubMenu', {
    extend: 'Uni.view.menu.SideMenu',
    xtype: 'ruleSetSubMenu',

    ruleSetId: null,

    title: Uni.I18n.translate('validation.validationRuleSet', 'CFG', 'Validation rule set'),

    initComponent: function () {
        var me = this;

        me.menuItems = [
            {
                text: Uni.I18n.translate('general.details', 'CFG', 'Details'),
                itemId: 'ruleSetOverviewLink',
                href: '#/administration/validation/rulesets/' + me.ruleSetId
            },           
            {
                text: Uni.I18n.translate('general.versions', 'CFG', 'Versions'),
                itemId: 'versionsLink',
                href: '#/administration/validation/rulesets/' + me.ruleSetId + '/versions'
            }
        ];

        me.callParent(arguments);
    }
});


