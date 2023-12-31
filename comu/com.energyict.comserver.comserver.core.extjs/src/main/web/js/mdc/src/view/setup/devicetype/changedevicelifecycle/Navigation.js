/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicetype.changedevicelifecycle.Navigation', {
    extend: 'Uni.view.menu.NavigationMenu',
    alias: 'widget.change-device-life-cycle-navigation',
    width: 256,
    jumpForward: false,
    jumpBack: false,
    ui: 'medium',
    title: Uni.I18n.translate('deviceLifeCycle.change', 'MDC', 'Change device life cycle'),
    items: [
        {
            itemId: 'select-device-life-cycle',
            action: 'selectDeviceLifeCycle',
            text: Uni.I18n.translate('deviceLifeCycle.select', 'MDC', 'Select device life cycle')
        },
        {
            itemId: 'status',
            action: 'status',
            text: Uni.I18n.translate('general.status', 'MDC', 'Status')
        }
    ]
});