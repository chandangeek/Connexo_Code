/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicezones.PreviewForm', {
    extend: 'Ext.form.Panel',
    xtype: 'devicezones-preview-form',

    border: false,
    itemId: 'deviceZonePreviewForm',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    defaults: {
        labelWidth: 150
    },

    items: [
        {
            xtype: 'displayfield',
            name: 'zoneTypeName',
            fieldLabel: Uni.I18n.translate('general.zoneType', 'MDC', 'Zone type'),
            itemId: 'zoneTypeName'
        },
    ]
});
