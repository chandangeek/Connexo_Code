/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.view.validation.RuleSubMenu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.ruleSubMenu',

    ruleSetId: null,
	versionId: null,
    ruleId: null,

    title: Uni.I18n.translate('validation.validationRule', 'CFG', 'Validation rule'),

    initComponent: function () {
        var me = this;

        me.menuItems = [
            {
                text: Uni.I18n.translate('general.details', 'CFG', 'Details'),
                itemId: 'ruleSetOverviewLink',
                href: '#/administration/validation/rulesets/' + me.ruleSetId + '/versions/' + me.versionId + '/rules/' + me.ruleId
            }
        ];

        me.callParent(arguments);
    }
});