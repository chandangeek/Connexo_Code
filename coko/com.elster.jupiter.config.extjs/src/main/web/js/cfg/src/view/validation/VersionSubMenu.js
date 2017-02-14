/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.view.validation.VersionSubMenu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.versionSubMenu',

    ruleSetId: null,
    versionId: null,

    title: Uni.I18n.translate('validation.validationRuleSetVersion', 'CFG', 'Validation rule set version'),

    initComponent: function () {
        var me = this;

        me.menuItems = [
            {
                text: Uni.I18n.translate('general.overview', 'CFG', 'Overview'),
                itemId: 'versionOverviewLink',
                href: '#/administration/validation/rulesets/' + me.ruleSetId + '/versions/' + me.versionId
            },
			{
                text: Uni.I18n.translate('general.validationRules', 'CFG', 'Validation rules'),
                itemId: 'versionValidationRulesLink',
                href: '#/administration/validation/rulesets/' + me.ruleSetId + '/versions/' + me.versionId + '/rules'
            }
        ];

        me.callParent(arguments);
    }
});