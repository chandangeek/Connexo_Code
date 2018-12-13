/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tme.view.relativeperiod.UsagePage', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.relative-period-usage',
    router: null,

    requires: [
        'Tme.view.relativeperiod.UsageGrid',
        'Tme.view.relativeperiod.CategoryFilter',
        'Uni.util.FormEmptyMessage'
    ],

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'relative-periods-menu',
                        itemId: 'relative-periods-menu',
                        router: me.router,
                        record: me.record,
                        toggle: 0
                    }
                ]
            }
        ];

        me.content = {
            title: Uni.I18n.translate('general.usage', 'TME', 'Usage'),
            xtype: 'panel',
            ui: 'large',
            items: [
                {
                    xtype: 'usage-category-filter'
                },
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'relative-period-usage-grid',
                        itemId: 'relative-period-usage-grid',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'uni-form-empty-message',
                        itemId: 'no-usage-found',
                        text: Uni.I18n.translate('relativeperiod.usage.empty', 'TME', 'There are no usages in the system')
                    }
                }
            ]

        };

        me.callParent(arguments);
    }
});