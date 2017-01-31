/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.readingtypes.view.bulk.Navigation', {
    extend: 'Uni.view.menu.NavigationMenu',
    alias: 'widget.reading-types-bulk-navigation',
    width: 256,
    jumpForward: false,
    jumpBack: true,
    ui: 'medium',
    title: Uni.I18n.translate('general.bulk', 'MTR', 'Bulk action'),
    items: [
        {
            itemId: 'select-reading-type',
            action: 'selectReadingType',
            text: Uni.I18n.translate('readingtypesmanagment.selectreadingtypes', 'MTR', 'Select reading types')
        },
        {
            itemId: 'select-action',
            action: 'selectDeviceLifeCycle',
            text: Uni.I18n.translate('readingtypesmanagment.selectaction', 'MTR', 'Select action')
        },
        {
            itemId: 'action-details',
            action: 'selectDeviceLifeCycle',
            text: Uni.I18n.translate('readingtypesmanagment.actiondetails', 'MTR', 'Action details')
        },
        {
            itemId: 'confirmation',
            action: 'status',
            text: Uni.I18n.translate('general.confirmation', 'MTR', 'Confirmation')
        },
        {
            itemId: 'status',
            action: 'status',
            text: Uni.I18n.translate('general.status', 'MTR', 'Status')
        }
    ]
});