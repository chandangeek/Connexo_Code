/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Pkj.view.bulk.Navigation', {
    extend: 'Uni.view.menu.NavigationMenu',
    alias: 'widget.certificates-bulk-navigation',
    width: 256,
    jumpForward: false,
    jumpBack: true,
    ui: 'medium',
    title: Uni.I18n.translate('general.bulk', 'PKJ', 'Bulk action'),
    items: [
        {
            itemId: 'select-reading-type',
            action: 'selectReadingType',
            text: Uni.I18n.translate('certificates.selectcertificates', 'PKJ', 'Select certificates')
        },
        {
            itemId: 'select-action',
            action: 'selectDeviceLifeCycle',
            text: Uni.I18n.translate('certificates.selectaction', 'PKJ', 'Select action')
        },
        {
            itemId: 'action-details',
            action: 'selectDeviceLifeCycle',
            text: Uni.I18n.translate('certificates.actiondetails', 'PKJ', 'Action details')
        },
        {
            itemId: 'confirmation',
            action: 'status',
            text: Uni.I18n.translate('general.confirmation', 'PKJ', 'Confirmation')
        },
        {
            itemId: 'status',
            action: 'status',
            text: Uni.I18n.translate('general.status', 'PKJ', 'Status')
        }
    ]
});