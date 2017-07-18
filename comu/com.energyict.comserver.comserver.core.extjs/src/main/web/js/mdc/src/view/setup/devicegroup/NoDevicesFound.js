/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicegroup.NoDevicesFound', {
    extend: 'Uni.view.notifications.NoItemsFoundPanel',
    alias: 'widget.no-devices-found-panel',
    title: Uni.I18n.translate('deviceGroup.noDevicesFoundPanel.title', 'MDC', 'No devices found'),
    reasons: [
        Uni.I18n.translate('deviceGroup.noDevicesFoundPanel.item1', 'MDC', 'There are no devices in the system.'),
        Uni.I18n.translate('deviceGroup.noDevicesFoundPanel.item2', 'MDC', 'No devices comply with the filter.')
    ],
    margin: '16 0 24 0'
});