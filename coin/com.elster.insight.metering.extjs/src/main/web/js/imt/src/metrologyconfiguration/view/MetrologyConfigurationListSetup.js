/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.metrologyconfiguration.view.MetrologyConfigurationListSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.metrologyConfigurationListSetup',
    requires: [
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Imt.metrologyconfiguration.view.MetrologyConfigurationList',
        'Imt.metrologyconfiguration.view.MetrologyConfigurationListPreview'
    ],
    router: null,

    initComponent: function () {
        var me = this;

        me.content = [
            {
                ui: 'large',
                title: Uni.I18n.translate('metrologyconfiguration.label.metrologyconfiguration.list', 'IMT', 'Metrology configurations'),

                items: [
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'metrologyConfigurationList',
                            itemId: 'metrologyConfigurationList',
                            router: me.router
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'ctr-no-metrology-configurations',
                            title: Uni.I18n.translate('metrologyconfiguration.list.empty', 'IMT', 'No metrology configurations found'),
                            reasons: [
                                Uni.I18n.translate('metrologyconfiguration.list.undefined', 'IMT', 'No metrology configurations have been defined yet.')
                            ]
                            // out of scope CXO-517
                            //stepItems: [
                            //    {
                            //        text: Uni.I18n.translate('metrologyconfiguration.general.add', 'IMT', 'Add metrology configuration'),
                            //        privileges: Imt.privileges.MetrologyConfig.admin,
                            //        href: '#/administration/metrologyconfiguration/add'
                            //    }
                            //]
                        },
                        previewComponent: {
                            xtype: 'metrology-config-details',
                            itemId: 'metrology-config-preview',
                            frame: true,
                            router: me.router
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});