/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.commands.view.CommandPreviewForm', {
    extend: 'Ext.form.Panel',
    frame: false,
    alias: 'widget.command-preview-form',
    requires: [
    ],

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
                    fieldLabel: Uni.I18n.translate('general.deviceName', 'MDC', 'Device name'),
                    name: 'parent',
                    renderer: function (value) {
                        return Ext.isEmpty(value) ? '-' : '<a href="#/devices/'+value.id+'">' + Ext.String.htmlEncode(value.id);
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('general.deviceType', 'MDC', 'Device type'),
                    name: 'deviceType',
                    renderer: function (deviceTypeIdAndName) {
                        return Ext.isEmpty(deviceTypeIdAndName) ? '-' :
                            '<a href="#/administration/devicetypes/'+deviceTypeIdAndName.id+'">' + Ext.String.htmlEncode(deviceTypeIdAndName.name);
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('general.deviceConfiguration', 'MDC', 'Device configuration'),
                    name: 'deviceTypeAndConfiguration',
                    renderer: function (deviceTypeAndConfig) {
                        return Ext.isEmpty(deviceTypeAndConfig) ? '-' :
                            '<a href="#/administration/devicetypes/'+deviceTypeAndConfig.deviceType.id+'/deviceconfigurations/'+deviceTypeAndConfig.deviceConfiguration.id+'">' + Ext.String.htmlEncode(deviceTypeAndConfig.deviceConfiguration.name);
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('general.commandName', 'MDC', 'Command name'),
                    dataIndex: 'messageSpecification',
                    flex: 1,
                    renderer: function (value) {
                        return Ext.isEmpty(value) ? '-' : value.name;
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('general.commandCategory', 'MDC', 'Command category'),
                    dataIndex: 'category'
                },
                {
                    fieldLabel: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                    dataIndex: 'status',
                    flex: 1,
                    renderer: function (value) {
                        return Ext.isEmpty(value) ? '-' : value.displayValue;
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('general.trackingSource', 'MDC', 'Tracking source'),
                    itemId: 'mdc-command-preview-tracking-field',
                    name: 'trackingIdAndName',
                    renderer: function (val) {
                        return Ext.isEmpty(val) || Ext.isEmpty(val.name) ? '-' : Ext.String.htmlEncode(val.name);
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('general.protocolInfo', 'MDC', 'Protocol info'),
                    name: 'errorMessage',
                    renderer: function (val) {
                        return Ext.isEmpty(val) ? '-' : Ext.String.htmlEncode(val);
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
                    renderer: function (value) {
                        return Ext.isEmpty(value) ? '-' : Ext.String.htmlEncode(value);
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('general.creationDate', 'MDC', 'Creation date'),
                    name: 'creationDate',
                    renderer: function (value) {
                        return Ext.isEmpty(value) ? '-' : Uni.DateTime.formatDateTimeLong(new Date(value));
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('general.releaseDate', 'MDC', 'Release date'),
                    name: 'releaseDate',
                    renderer: function (value) {
                        return Ext.isEmpty(value) ? '-' : Uni.DateTime.formatDateTimeLong(new Date(value));
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('general.sentDate', 'MDC', 'Sent date'),
                    name: 'sentDate',
                    renderer: function (value) {
                        return Ext.isEmpty(value) ? '-' : Uni.DateTime.formatDateTimeLong(new Date(value));
                    }
                }
            ]
        }
    ]

});