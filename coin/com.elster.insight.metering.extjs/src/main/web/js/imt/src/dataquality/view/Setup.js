/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.dataquality.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.imt-quality-setup',
    requires: [
        'Imt.dataquality.view.Grid',
        'Imt.dataquality.view.Preview',
        'Imt.dataquality.view.Filter',
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
                itemId: 'imt-quality-setup-panel',
                title: Uni.I18n.translate('general.dataQuality', 'IMT', 'Data quality'),
                items: [
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'imt-quality-grid',
                            itemId: 'imt-quality-grid',
                            router: me.router
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'imt-quality-empty',
                            title: Uni.I18n.translate('dataQuality.empty.title', 'IMT', 'No usage points found'),
                            reasons: [
                                Uni.I18n.translate('dataQuality.empty.list.item1', 'IMT', 'There are no usage points in the system'),
                                Uni.I18n.translate('dataQuality.empty.list.item2', 'IMT', 'The filter is too narrow')
                            ]
                        },
                        previewComponent: {
                            xtype: 'imt-quality-preview',
                            itemId: 'imt-quality-preview',
                            router: me.router
                        }
                    }
                ],
                dockedItems: [
                    {
                        dock: 'top',
                        itemId: 'imt-quality-filter',
                        xtype: 'imt-quality-filter',
                        filterDefault: me.filterDefault,
                        hasDefaultFilters: true
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});