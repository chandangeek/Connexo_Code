/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.searchitems.SortMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.items-sort-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('searchItems.name', 'MDC', 'Name'),
            value: 'name'
        },
        {
            text: Uni.I18n.translate('searchItems.serialNumber', 'MDC', 'Serial number'),
            value: 'serialNumber'
        },
        {
            text: Uni.I18n.translate('general.type', 'MDC', 'Type'),
            value: 'deviceConfiguration.deviceType.name'
        },
        {
            text: Uni.I18n.translate('searchItems.configuration', 'MDC', 'Configuration'),
            value: 'deviceConfiguration.name'
        }
    ]
});