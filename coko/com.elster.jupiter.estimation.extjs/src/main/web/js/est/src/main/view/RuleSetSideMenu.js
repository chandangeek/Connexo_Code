/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Est.main.view.RuleSetSideMenu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.estimation-rule-set-side-menu',
    router: null,
    sharedForMdc: true,
    title: Uni.I18n.translate('estimationrulesets.estimationruleset', 'EST', 'Estimation rule set'),

    initComponent: function () {
        var me = this;

        me.menuItems = [
            {
                text: Uni.I18n.translate('estimationrulesets.estimationruleset', 'EST', 'Estimation rule set'),
                itemId: 'estimation-rule-set-link',
                href: me.router.getRoute('administration/estimationrulesets/estimationruleset').buildUrl()
            },
            {
                text: Uni.I18n.translate('general.estimationRules', 'EST', 'Estimation rules'),
                itemId: 'estimation-rules-link',
                href: me.router.getRoute('administration/estimationrulesets/estimationruleset/rules').buildUrl()
            }
        ];

        me.callParent(arguments);
    }
});