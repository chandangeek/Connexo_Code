/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comservercomports.forms.COAP', {
    extend: 'Ext.form.Panel',
    alias: 'widget.comPortFormCOAP',
    layout: 'column',
    defaults: {
        xtype: 'container',
        layout: 'form',
        columnWidth: 0.5
    },
    ui: 'medium',

    items: [
        {
            defaults: {
                xtype: 'displayfield',
                labelWidth: 300
            },
            items: [
                {
                    fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                    name: 'name'
                },
                {
                    fieldLabel: Uni.I18n.translate('general.comServer', 'MDC', 'Communication server'),
                    name: 'comServerName'
                },
                {
                    fieldLabel: Uni.I18n.translate('comports.preview.direction', 'MDC', 'Direction'),
                    name: 'direction'
                },
                {
                    fieldLabel: Uni.I18n.translate('comports.preview.simultaneousConnections', 'MDC', 'Simultaneous connections'),
                    name: 'numberOfSimultaneousConnections'
                },
                {
                    fieldLabel: Uni.I18n.translate('comports.preview.portNumber', 'MDC', 'Port number'),
                    name: 'portNumber'
                }
            ]
        },
        {
            defaults: {
                xtype: 'displayfield',
                labelWidth: 300
            },
            items: [
                {
                    fieldLabel: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                    name: 'status'
                },
                {
                    fieldLabel: Uni.I18n.translate('general.type', 'MDC', 'Type'),
                    name: 'comPortType',
                    renderer: function (value) {
                        return value && value.localizedValue;
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('general.comPortPools', 'MDC', 'Communication port pools'),
                    name: 'inboundComPortPools',
                    htmlEncode: false
                },
                {
                    fieldLabel: Uni.I18n.translate('comports.preview.contextPath', 'MDC', 'Context path'),
                    name: 'contextPath'
                },
                {
                    fieldLabel: Uni.I18n.translate('comServerComPorts.form.dtls', 'MDC', 'Use dtls'),
                    name: 'useDtls',
                    renderer: function (value) {
                        return value ? Uni.I18n.translate('general.yes', 'MDC', 'Yes') : Uni.I18n.translate('general.no', 'MDC', 'No');
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('comServerComPorts.form.sharedKeys', 'MDC', 'Use pre shared keys'),
                    name: 'useSharedKeys',
                    hidden: true,
                    renderer: function (value) {
                        return value ? Uni.I18n.translate('general.yes', 'MDC', 'Yes') : Uni.I18n.translate('general.no', 'MDC', 'No');
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('comports.preview.keyStoreFilePath', 'MDC', 'Key store file path'),
                    name: 'keyStoreFilePath'
                },
                {
                    xtype: 'fieldcontainer',
                    itemId: 'mdc-coap-port-preview-keyStoreAccessPasswordContainer',
                    fieldLabel: Uni.I18n.translate('comports.preview.keyStoreAccessPassword', 'MDC', 'Key store access password'),
                    items: [
                        {
                            xtype: 'displayfield',
                            name: 'keyStorePassword',
                            hidden: true,
                            renderer: function (value) {
                                return value ? Ext.String.htmlEncode(value) : '******';
                            }
                        },
                        {
                            xtype: 'checkbox',
                            checked: false,
                            action: 'passwordVisibleTrigger',
                            boxLabel: Uni.I18n.translate('comports.preview.seePassword', 'MDC', 'See password'),
                            margin: '0 0 10 0'
                        }
                    ]
                },
                {
                    fieldLabel: Uni.I18n.translate('comports.preview.trustStoreFilePath', 'MDC', 'Trust store file path'),
                    name: 'trustStoreFilePath'
                },
                {
                    xtype: 'fieldcontainer',
                    itemId: 'mdc-coap-port-preview-trustStoreAccessPasswordContainer',
                    fieldLabel: Uni.I18n.translate('comports.preview.trustStoreAccessPassword', 'MDC', 'Trust store access password'),
                    items: [
                        {
                            xtype: 'displayfield',
                            name: 'trustStorePassword',
                            hidden: true,
                            renderer: function (value) {
                                return value ? Ext.String.htmlEncode(value) : '******';
                            }
                        },
                        {
                            xtype: 'checkbox',
                            checked: false,
                            action: 'passwordVisibleTrigger',
                            boxLabel: Uni.I18n.translate('comports.preview.seePassword', 'MDC', 'See password'),
                            margin: '0 0 10 0'
                        }
                    ]
                }
            ]
        }
    ]
});