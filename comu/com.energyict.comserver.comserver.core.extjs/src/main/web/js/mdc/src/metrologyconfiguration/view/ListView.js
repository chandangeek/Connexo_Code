/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.metrologyconfiguration.view.ListView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.metrology-configurations-list-view',
    requires: [
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.metrologyconfiguration.view.MetrologyConfigurationsGrid',
        'Mdc.metrologyconfiguration.view.MetrologyConfigurationDetails'
    ],

    router: null,

    initComponent: function () {
        var me = this;

        me.content = [
            {
                ui: 'large',
                title: Uni.I18n.translate('general.metrologyConfigurations', 'MDC', 'Metrology configurations'),
                itemId: 'metrology-configurations-list-view-main',
                items: [
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'metrology-configurations-grid',
                            itemId: 'metrology-configurations-grid',
                            router: me.router
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'no-metrology-configurations',
                            title: Uni.I18n.translate('metrologyconfiguration.list.empty', 'MDC', 'No metrology configurations found'),
                            reasons: [
                                Uni.I18n.translate('metrologyconfiguration.list.item1', 'MDC', 'No metrology configurations have been defined yet.'),
                                Uni.I18n.translate('metrologyconfiguration.list.item2', 'MDC', 'Metrology configurations exist but you don\'t have permission to view them.')
                            ],
                            stepItems: [
                                {
                                    xtype: 'button',
                                    itemId: 'empty-msg-add-add-metrology-configuration-btn',
                                    text: Uni.I18n.translate('general.addMetrologyConfiguration', 'MDC', 'Add metrology configuration'),
                                    privileges: Mdc.privileges.MetrologyConfiguration.canAdmin(),
                                    href: me.router.getRoute('administration/metrologyconfiguration/add').buildUrl()
                                }
                            ]
                        },
                        previewComponent: {
                            xtype: 'metrology-configuration-details',
                            itemId: 'metrology-configuration-preview',
                            frame: true
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});