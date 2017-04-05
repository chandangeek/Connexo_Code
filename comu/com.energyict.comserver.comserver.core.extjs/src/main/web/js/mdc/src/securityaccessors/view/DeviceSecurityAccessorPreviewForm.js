/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.view.DeviceSecurityAccessorPreviewForm', {
    extend: 'Ext.panel.Panel',
    frame: false,
    alias: 'widget.device-security-accessor-preview-form',
    layout: 'fit',

    items: {
        xtype: 'form',
        layout: 'column',
        defaults: {
            xtype: 'container',
            layout: 'form',
            columnWidth: 0.5
        },
        items: [
            {
                defaults: {
                    xtype: 'displayfield'
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                        name: 'name'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.description', 'MDC', 'Description'),
                        name: 'description'
                    }
                ]
            },
            {
                defaults: {
                    xtype: 'displayfield'
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('general.lastReadDate', 'MDC', 'Last read date'),
                        name: 'lastReadDate'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.validUntil', 'MDC', 'Valid until'),
                        name: 'expirationTime',
                        renderer: function(value){
                            if (Ext.isEmpty(value)) {
                                return '-';
                            }
                            return Uni.DateTime.formatDateShort(new Date(value));
                        }
                    }
                ]
            }
        ]
    }

});
