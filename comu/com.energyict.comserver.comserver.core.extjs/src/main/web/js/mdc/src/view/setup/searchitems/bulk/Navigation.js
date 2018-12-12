/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.searchitems.bulk.Navigation', {
    extend: 'Uni.view.menu.NavigationMenu',
    alias: 'widget.searchitems-bulk-navigation',
    width: 200,
    jumpForward: false,
    jumpBack: false,
    ui: 'medium',
    title: Uni.I18n.translate('general.bulkAction', 'MDC', 'Bulk action'),
    items: [
        {
            itemId: 'SelectDevices',
            text: Uni.I18n.translate('searchItems.bulk.selectDevices', 'MDC', 'Select devices')
        },
        {
            itemId: 'SelectAction',
            text: Uni.I18n.translate('searchItems.bulk.selectAction', 'MDC', 'Select action')
        },
        {
            itemId: 'actionDetails',
            text:  Uni.I18n.translate('searchItems.bulk.actionDetails', 'MDC', 'Action details')
        },
        {   itemId: 'Confirmation',
            text: Uni.I18n.translate('searchItems.bulk.confirmation', 'MDC', 'Confirmation')
        },
        {
            itemId: 'Status',
            text: Uni.I18n.translate('general.status', 'MDC', 'Status')
        }
    ]

});