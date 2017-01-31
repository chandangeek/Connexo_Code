/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Est.estimationrules.view.SideMenu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.estimation-rule-side-menu',
    router: null,
    title: Uni.I18n.translate('general.estimationRule', 'EST', 'Estimation rule'),

    initComponent: function () {
        var me = this;

        me.menuItems = [
            {
                text: Uni.I18n.translate('general.estimationRule', 'EST', 'Estimation rule'),
                itemId: 'estimation-rule-link',
                href: me.router.getRoute('administration/estimationrulesets/estimationruleset/rules/rule').buildUrl()
            }
        ];

        me.callParent(arguments);
    }
});