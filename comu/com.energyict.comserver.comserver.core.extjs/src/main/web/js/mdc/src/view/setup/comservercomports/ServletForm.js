/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comservercomports.ServletForm', {
    extend: 'Ext.container.Container',
    alias: 'widget.servletForm',
    defaults: {
        labelWidth: 250,
        width: 600,
        validateOnChange: false,
        validateOnBlur: false
    },
    requires: [
        'Uni.form.field.Password',
        'Mdc.view.setup.comservercomports.InboundComPortPoolCombo'
    ],
    items: [
        {
            xtype: 'numberfield',
            fieldLabel: Uni.I18n.translate('comServerComPorts.form.connectionCount', 'MDC', 'Simultaneous connections'),
            required: true,
            allowBlank: false,
            name: 'numberOfSimultaneousConnections',
            minValue: 0,
            stripCharsRe: /\D/,
            listeners: {
                blur: {
                    fn: function(field){
                        if(Ext.isEmpty(field.getValue())) {
                            field.setValue(1);
                        }
                    }
                }
            },
            value: 1,
            width: 370
        },
        {
            xtype: 'numberfield',
            fieldLabel: Uni.I18n.translate('comServerComPorts.form.portNum', 'MDC', 'Port number'),
            required: true,
            allowBlank: false,
            name: 'portNumber',
            minValue: 0,
            stripCharsRe: /\D/,
            listeners: {
                blur: {
                    fn: function(field){
                        if(Ext.isEmpty(field.getValue())) {
                            field.setValue(1);
                        }
                    }
                }
            },
            value: 0,
            width: 370
        },
        {
            xtype: 'inbound-com-port-pool-combo',
            fieldLabel: Uni.I18n.translate('general.comPortPool', 'MDC', 'Communication port pool'),
            required: false,
            store: 'Mdc.store.InboundComPortPools',
            editable: false,
            queryMode: 'local',
            itemId: 'inboundPool',
            name: 'comPortPool_id',
            displayField: 'name',
            valueField: 'id',
            emptyText: Uni.I18n.translate('comports.selectInboundPool','MDC','Select inbound communication port pool...')
        },
        {
            xtype: 'fieldcontainer',
            fieldLabel: '&nbsp;',
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            items: [
                {
                    html: '<span style="color: grey"><i>' + Uni.I18n.translate('comports.preview.noInboundCommPortPool', 'MDC', 'When no inbound communication port pool is selected,<br> the port cannot be activated') + '</i></span>',
                    xtype: 'component'

                }
            ]
        },
        {
            xtype: 'textfield',
            required: true,
            allowBlank: false,
            fieldLabel: Uni.I18n.translate('comServerComPorts.form.contextPath', 'MDC', 'Context path'),
            name: 'contextPath',
            vtype: 'checkURLForBlacklistCharacters'
        },
        {
            xtype: 'checkbox',
            name: 'useHttps',
            fieldLabel: ' ',
            boxLabel: Uni.I18n.translate('comServerComPorts.form.https', 'MDC', 'Use https')
        },
        {
            xtype: 'textfield',
            required: true,
            allowBlank: false,
            fieldLabel: Uni.I18n.translate('comServerComPorts.form.keyStorePath', 'MDC', 'Key store file path'),
            itemId: 'keyStoreFilePath',
            disabled: true,
            name: 'keyStoreFilePath',
            vtype: 'checkForPathBlacklistCharacters'
        },
        {
            xtype: 'password-field',
            required: true,
            fieldLabel: Uni.I18n.translate('comServerComPorts.form.keyStorePass', 'MDC', 'Key store access password'),
            itemId: 'keyStorePassword',
            disabled: true,
            name: 'keyStorePassword'
        },
        {
            xtype: 'textfield',
            required: true,
            allowBlank: false,
            disabled: true,
            fieldLabel: Uni.I18n.translate('comServerComPorts.form.trustStorePath', 'MDC', 'Trust store file path'),
            itemId: 'trustStoreFilePath',
            name: 'trustStoreFilePath',
            vtype: 'checkForPathBlacklistCharacters'
        },
        {
            xtype: 'password-field',
            required: true,
            disabled: true,
            fieldLabel: Uni.I18n.translate('comServerComPorts.form.trustStorePass', 'MDC', 'Trust store access password'),
            itemId: 'trustStorePassword',
            name: 'trustStorePassword'
        }
    ],

    showInbound: function () {
    },
    showOutbound: function () {
    }
});

