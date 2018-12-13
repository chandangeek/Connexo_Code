/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tme.view.relativeperiod.Menu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.relative-periods-menu',

    router: null,

    title: Uni.I18n.translate('general.relativePeriod', 'TME', 'Relative period'),
    objectType: Uni.I18n.translate('general.relativePeriod', 'TME', 'Relative period'),

    initComponent: function () {
        var me = this;

        me.menuItems = [
            {
                text: Uni.I18n.translate('general.details', 'TME', 'Details'),
                itemId: 'relative-period-overview-link',
                href: me.router.getRoute('administration/relativeperiods/relativeperiod').buildUrl()
            },
            {
                text: Uni.I18n.translate('general.usage', 'TME', 'Usage'),
                itemId: 'relative-period-usage-link',
                href: me.router.getRoute('administration/relativeperiods/relativeperiod/usage').buildUrl()
            }
        ];

        me.callParent(arguments);
    }
});


