/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tme.view.relativeperiod.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.relative-periods-setup',

    router: null,

    requires: [
        'Tme.privileges.Period',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Tme.view.relativeperiod.Grid',
        'Tme.view.relativeperiod.Preview'
    ],

    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('general.relativePeriods', 'TME', 'Relative periods'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'relative-periods-grid',
                        itemId: 'grd-relative-periods',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'ctr-no-relative-period',
                        title: Uni.I18n.translate('relativePeriods.empty.title', 'TME', 'No relative periods found'),
                        reasons: [
                            Uni.I18n.translate('relativePeriods.empty.list.item1', 'TME', 'No relative periods have been defined yet.'),
                            Uni.I18n.translate('relativePeriods.empty.list.item2', 'TME', 'Relative periods exist, but you do not have permission to view them.')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('relativeperiod.add', 'TME', 'Add relative period'),
                                privileges: Tme.privileges.Period.admin,
                                href: typeof me.router.getRoute('administration/relativeperiods/add') !== 'undefined'
                                    ? me.router.getRoute('administration/relativeperiods/add').buildUrl() : null
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'relative-periods-preview',
                        itemId: 'pnl-relative-periods-preview'
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});
