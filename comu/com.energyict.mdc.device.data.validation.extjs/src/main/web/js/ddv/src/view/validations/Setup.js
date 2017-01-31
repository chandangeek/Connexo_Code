/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Ddv.view.validations.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.ddv-validations-setup',
    requires: [
        'Ddv.view.validations.Grid',
        'Ddv.view.validations.Preview',
        'Ddv.view.validations.Filter',
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
                title: Uni.I18n.translate('validation.validations.title', 'DDV', 'Validations'),
                items: [
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'ddv-validations-grid',
                            itemId: 'validations-grid',
                            router: me.router
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'ctr-no-validations',
                            title: Uni.I18n.translate('validations.empty.title', 'DDV', 'No devices with suspects found'),
                            reasons: [
                                Uni.I18n.translate('validations.empty.list.item1', 'DDV', 'Data has not been validated yet'),
                                Uni.I18n.translate('validations.empty.list.item2', 'DDV', 'Data has been successfully validated'),
                                Uni.I18n.translate('validations.empty.list.item3', 'DDV', 'No devices with suspects comply with the filter')
                            ]
                        },
                        previewComponent: {
                            xtype: 'ddv-validations-preview',
                            itemId: 'preview-validations'
                        }
                    }
                ],
                dockedItems: [
                    {
                        dock: 'top',
                        itemId: 'ddv-validations-filter-panel-top',
                        xtype: 'ddv-validations-filter',
                        filterDefault: me.filterDefault,
                        hasDefaultFilters: true
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});