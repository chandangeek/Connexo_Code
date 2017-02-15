/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.view.communicationsbulk.Navigation', {
    extend: 'Uni.view.menu.NavigationMenu',
    alias: 'widget.communications-bulk-navigation',
    jumpForward: false,
    jumpBack: true,
    ui: 'medium',
    title: Uni.I18n.translate('general.bulkAction', 'DSH', 'Bulk action'),
    items: [
        {
            itemId: 'cmbn-select-connections',
            action: 'select-communication',
            text: Uni.I18n.translate('communication.bulk.selectCommunications', 'DSH', 'Select communications')
        },
        {
            itemId: 'cmbn-select-action',
            action: 'select-action',
            text: Uni.I18n.translate('general.selectAction', 'DSH', 'Select action')
        },
        {
            itemId: 'cmbn-confirmation',
            action: 'confirmation',
            text: Uni.I18n.translate('general.confirmation', 'DSH', 'Confirmation')
        },
        {
            itemId: 'cmbn-status',
            action: 'status',
            text: Uni.I18n.translate('general.status', 'DSH', 'Status')
        }
    ]
});