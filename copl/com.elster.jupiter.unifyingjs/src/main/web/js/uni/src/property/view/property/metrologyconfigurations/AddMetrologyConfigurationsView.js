/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.metrologyconfigurations.AddMetrologyConfigurationsView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.uni-add-metrology-configurations-view',
    requires: [
        'Uni.view.container.EmptyGridContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        //'Uni.property.view.property.deviceconfigurations.AddDeviceConfigurationsGrid'
        'Uni.property.view.property.metrologyconfigurations.AddMetrologyConfigurationsGrid'
    ],
    content: [
        {
            ui: 'large',
            itemId: 'uni-add-metrology-configurations-panel',
            title: Uni.I18n.translate('metrologyconfigurations.addMetrologyConfigurations', 'UNI', 'Add metrology configurations'),
            items: [
                {
                    xtype: 'emptygridcontainer',
                    grid: {
                        xtype: 'uni-add-metrology-configurations-grid',
                        itemId: 'uni-add-metrology-configurations-grid'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'uni-add-metrology-configurations-no-items-found-panel',
                        title: Uni.I18n.translate('metrologyconfigurations.empty.metrologyconfiguration.title', 'UNI', 'No metrology configurations found'),
                        reasons: [
                            Uni.I18n.translate('metrologyconfigurations.empty.list.item1', 'UNI', 'No metrology configurations have been added yet.'),
                            Uni.I18n.translate('metrologyconfigurations.empty.list.item2', 'UNI', 'Metrology configurations exist, but you do not have permission to view them.')
                        ]
                    }
                }
            ]
        }
    ]
});