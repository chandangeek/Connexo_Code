/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.metrologyconfiguration.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.metrology-configuration-setup',
    requires: [
        'Imt.metrologyconfiguration.view.MetrologyConfigurationPurposes',
        'Imt.metrologyconfiguration.view.MetrologyConfigurationSideMenu',
        'Imt.metrologyconfiguration.view.MetrologyConfigurationDetailsForm',
        'Imt.metrologyconfiguration.view.MetrologyConfigurationActionMenu'
    ],
    router: null,
    metrologyConfig: null,

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'panel',
                itemId: 'metrology-config-setup-panel',
                ui: 'large',
                title: Uni.I18n.translate('general.overview', 'IMT', 'Overview'),
                tools: [
                    {
                        xtype: 'uni-button-action',
                        disabled: me.metrologyConfig.get('status').id == 'deprecated',
                        privileges: Imt.privileges.MetrologyConfig.admin,
                        menu: {
                            xtype: 'metrology-configuration-action-menu',
                            itemId: 'metrology-configuration-action-menu',
                            record: me.metrologyConfig
                        }
                    }
                ],
                items: [
                    {
                        xtype: 'metrology-config-details-form',
                        itemId: 'metrology-config-setup-general-info',
                        displayPurposes: false,
                        ui: 'tile',
                        title: Uni.I18n.translate('general.generalInformation', 'IMT', 'General information'),
                        margin: 0
                    },
                    {
                        xtype: 'panel',
                        itemId: 'metrology-config-purposes-panel',
                        ui: 'medium',
                        title: Uni.I18n.translate('general.purposes', 'IMT', 'Purposes'),
                        padding: 0,
                        margin: '20 0 0 0',
                        bbar: {
                            xtype: 'metrology-config-purposes',
                            itemId: 'metrology-config-purposes',
                            metrologyConfig: me.metrologyConfig
                        }
                    }
                ]
            }
        ];

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'metrology-configuration-side-menu',
                        itemId: 'metrology-configuration-side-menu',
                        router: me.router,
                        metrologyConfig: me.metrologyConfig
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});