/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.registereddevices.view.RegisteredDevicesKPIs', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.registered-devices-kpis-view',

    requires: [
        'Mdc.registereddevices.view.RegisteredDevicesKPIsGrid',
        'Mdc.registereddevices.view.RegisteredDevicesKPIPreview',
        'Mdc.privileges.RegisteredDevicesKpi',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('general.registeredDevicesKPIs', 'MDC', 'Registered devices KPIs'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'registered-devices-kpis-grid',
                        itemId: 'mdc-registered-devices-kpis-grid'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'mdc-no-registered-devices-kpis',
                        title: Uni.I18n.translate('registeredDevicesKPIs.empty.title', 'MDC', 'No registered devices KPIs found'),
                        reasons: [
                            Uni.I18n.translate('registeredDevicesKPIs.empty.list.item', 'MDC', 'No registered devices KPIs have been defined yet.')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('registeredDevicesKPIs.add', 'MDC', 'Add registered devices KPI'),
                                itemId: 'mdc-no-registered-devices-kpis-add',
                                action: 'addRegisteredDevicesKpi',
                                privileges: Mdc.privileges.RegisteredDevicesKpi.admin
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'registered-devices-kpi-preview',
                        itemId: 'mdc-registered-devices-kpi-preview'
                    }
                }
            ]
        }
    ]
});
