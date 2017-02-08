/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.servicecategories.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.service-categories-setup',
    requires: [
        'Imt.servicecategories.view.ServiceCategoriesGrid',
        'Imt.servicecategories.view.CASpanel',
        'Uni.view.container.PreviewContainer',
        'Uni.util.FormEmptyMessage',
        'Imt.servicecategories.view.ServiceCategoryDetailForm'
    ],

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'panel',
                itemId: 'service-categories-panel',
                title: Uni.I18n.translate('general.serviceCategories', 'IMT', 'Service categories'),
                ui: 'large',
                items: {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'service-categories-grid',
                        itemId: 'service-categories-grid'
                    },
                    emptyComponent: {
                        xtype: 'uni-form-empty-message',
                        itemId: 'no-service-categories-found-panel',
                        text: Uni.I18n.translate('serviceCategories.empty.list.item', 'IMT', 'No service categories defined yet.')
                    },
                    previewComponent: {
                        xtype: 'container',
                        items: [
                            {
                                xtype: 'service-category-detail-form',
                                itemId: 'service-category-preview',
                                frame: true,
                                title: ' '
                            },
                            {
                                xtype: 'service-categories-cas-panel',
                                itemId: 'service-categories-cas-panel',
                                title: Uni.I18n.translate('metrologyconfiguration.label.CAS', 'IMT', 'Custom attribute sets'),
                                ui: 'medium',
                                padding: 0
                            }
                        ]
                    }
                }
            }
        ];

        me.callParent(arguments);
    }
});