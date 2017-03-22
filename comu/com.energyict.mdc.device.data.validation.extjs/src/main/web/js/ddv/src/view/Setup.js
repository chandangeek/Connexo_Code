/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Ddv.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.ddv-quality-setup',
    requires: [
        'Ddv.view.Grid',
        'Ddv.view.Preview',
        'Ddv.view.Filter',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],
    router: null,
    filterDefault: {},
    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                itemId: 'ddv-quality-setup-panel',
                title: Uni.I18n.translate('general.dataQuality', 'DDV', 'Data quality'),
                items: [
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'ddv-quality-grid',
                            itemId: 'ddv-quality-grid',
                            router: me.router
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'ddv-quality-empty',
                            title: Uni.I18n.translate('dataQuality.empty.title', 'DDV', 'No devices found'),
                            reasons: [
                                Uni.I18n.translate('dataQuality.empty.list.item1', 'DDV', 'There are no devices in the system'),
                                Uni.I18n.translate('dataQuality.empty.list.item2', 'DDV', 'The filter is too narrow')
                            ]
                        },
                        previewComponent: {
                            xtype: 'ddv-quality-preview',
                            itemId: 'ddv-quality-preview',
                            router: me.router
                        }
                    }
                ],
                dockedItems: [
                    {
                        dock: 'top',
                        itemId: 'ddv-quality-filter',
                        xtype: 'ddv-quality-filter',
                        filterDefault: me.filterDefault,
                        hasDefaultFilters: true
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});