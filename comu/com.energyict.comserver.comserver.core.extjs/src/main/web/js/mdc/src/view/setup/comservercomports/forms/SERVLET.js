Ext.define('Mdc.view.setup.comservercomports.forms.SERVLET', {
    extend: 'Ext.form.Panel',
    alias: 'widget.comPortFormSERVLET',
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
                labelWidth: 250
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
                labelWidth: 250
            },
            items: [
                {
                    fieldLabel: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                    name: 'status'
                },
                {
                    fieldLabel: Uni.I18n.translate('general.type', 'MDC', 'Type'),
                    name: 'comPortType'
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
                    fieldLabel: Uni.I18n.translate('comports.preview.keyStoreFilePath', 'MDC', 'Key store file path'),
                    name: 'keyStoreFilePath'
                },
                {
                    xtype: 'fieldcontainer',
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
                            xtype: 'container',
                            layout: 'hbox',
                            items: [
                                {
                                    xtype: 'checkbox',
                                    checked: false,
                                    action: 'passwordVisibleTrigger',
                                    margin: '0 10 0 0'
                                },
                                {
                                    xtype: 'displayfield',
                                    renderer: function () {
                                        return Uni.I18n.translate('comports.preview.seePassword', 'MDC', 'See password')
                                    }
                                }
                            ]
                        }
                    ]
                },
                {
                    fieldLabel: Uni.I18n.translate('comports.preview.trustStoreFilePath', 'MDC', 'Trust store file path'),
                    name: 'trustStoreFilePath'
                },
                {
                    xtype: 'fieldcontainer',
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
                            xtype: 'container',
                            layout: 'hbox',
                            items: [
                                {
                                    xtype: 'checkbox',
                                    checked: false,
                                    action: 'passwordVisibleTrigger',
                                    margin: '0 10 0 0'
                                },
                                {
                                    xtype: 'displayfield',
                                    renderer: function () {
                                        return Uni.I18n.translate('comports.preview.seePassword', 'MDC', 'See password')
                                    }
                                }
                            ]
                        }
                    ]
                }
            ]
        }
    ]
});