/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.view.datapurge.LogPreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.data-purge-log-preview-form',
    defaults: {
        xtype: 'displayfield',
        labelWidth: 200
    },
    items: [
        {
            fieldLabel: Uni.I18n.translate('datapurge.log.form.startedon', 'SAM', 'Data purge task started on'),
            name: 'startDate',
            renderer: function (value) {
                return value
                    ? Uni.I18n.translate('general.dateAtTime', 'SAM', '{0} at {1}',
                        [Uni.DateTime.formatDateLong(value),Uni.DateTime.formatTimeLong(value)])
                    : '';
            }
        },
        {
            fieldLabel: Uni.I18n.translate('general.status', 'SAM', 'Status'),
            name: 'status'
        }
    ]
});