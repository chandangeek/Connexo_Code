/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicecommand.DeviceCommandPreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.deviceCommandPreviewForm',
    layout: {
        type: 'column'
    },
    items: [
        {
            columnWidth: 0.5,
            defaults: {
                xtype: 'displayfield',
                labelWidth: 200
            },
            items: [
                {
                    fieldLabel: Uni.I18n.translate('general.commandName', 'MDC', 'Command name'),
                    name: 'messageSpecification',
                    renderer: function (val) {
                        return val ? Ext.String.htmlEncode(val.name) : '-'
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('general.commandCategory', 'MDC', 'Command category'),
                    name: 'category',
                    renderer: function (val) {
                        return val ? Ext.String.htmlEncode(val) : '-'
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                    name: 'status',
                    renderer: function (val) {
                        return val.displayValue ? Ext.String.htmlEncode(val.displayValue): '-'
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('general.serviceCall', 'MDC', 'Service call'),
                    name: 'trackingIdAndName',
                    itemId: 'tracking',
                    renderer: function (val) {
                        return val ? Ext.String.htmlEncode(val) : '-'
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('general.errorMessage', 'MDC', 'Error message'),
                    name: 'errorMessage',
                    renderer: function (val) {
                        return val ? Ext.String.htmlEncode(val) : '-'
                    }
                }
            ]
        },
        {
            columnWidth: 0.5,
            defaults: {
                xtype: 'displayfield',
                labelWidth: 200
            },
            items: [
                {
                    fieldLabel: Uni.I18n.translate('general.createdBy', 'MDC', 'Created by'),
                    name: 'user',
                    renderer: function (val) {
                        return val ? Ext.String.htmlEncode(val) : '-'
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('general.creationDate', 'MDC', 'Creation date'),
                    name: 'creationDate',
                    renderer: function (value) {
                        return value ? Uni.DateTime.formatDateTimeLong(new Date(value)) : '-';
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('general.releaseDate', 'MDC', 'Release date'),
                    name: 'releaseDate',
                    renderer: function (value) {
                        return value ? Uni.DateTime.formatDateTimeLong(new Date(value)) : '-';
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('general.sentDate', 'MDC', 'Sent date'),
                    name: 'sentDate',
                    renderer: function (value) {
                        return value ? Uni.DateTime.formatDateTimeLong(new Date(value)) : '-';
                    }
                }
            ]
        }
    ]
});

