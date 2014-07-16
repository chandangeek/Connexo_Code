Ext.define('Mdc.view.setup.comservercomports.ServletForm', {
    extend: 'Ext.container.Container',
    alias: 'widget.servletForm',
    defaults: {
        labelWidth: 250,
        width: 600,
        validateOnChange: false,
        validateOnBlur: false
    },
    items: [
        {
            xtype: 'numberfield',
            fieldLabel: Uni.I18n.translate('comServerComPorts.form.connectionCount', 'MDC', 'Simultaneous connections'),
            required: true,
            allowBlank: false,
            name: 'numberOfSimultaneousConnections',
            minValue: 1,
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
            width: 350
        },
        {
            xtype: 'numberfield',
            fieldLabel: Uni.I18n.translate('comServerComPorts.form.portNum', 'MDC', 'Port number'),
            required: true,
            allowBlank: false,
            name: 'portNumber',
            minValue: 1,
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
            width: 350
        },
        {
            xtype: 'combobox',
            fieldLabel: Uni.I18n.translate('comServerComPorts.form.inPools', 'MDC', 'Communication port pool'),
            required: false,
            store: 'Mdc.store.InboundComPortPools',
            editable: false,
            queryMode: 'local',
            itemId: 'inboundPool',
            name: 'comPortPool_id',
            displayField: 'name',
            valueField: 'id',
            emptyText: 'Select inbound communication pool'
        },
        {
            xtype: 'textfield',
            required: true,
            allowBlank: false,
            fieldLabel: Uni.I18n.translate('comServerComPorts.form.contextPath', 'MDC', 'Context path'),
            name: 'contextPath'
        },
        {
            xtype: 'fieldcontainer',
            fieldLabel: 'Https',
            name: 'https',
            items: [
                {
                    xtype: 'container',
                    margin: '0 0 8 0',
                    layout: 'hbox',
                    items: [
                        {
                            xtype: 'checkbox',
                            name: 'useHttps',
                            margin: '0 16 0 0'
                        },
                        {
                            xtype: 'label',
                            text: Uni.I18n.translate('comServerComPorts.form.https', 'MDC', 'Https')
                        }
                    ]
                }
            ]
        },
        {
            xtype: 'textfield',
            required: true,
            allowBlank: false,
            fieldLabel: Uni.I18n.translate('comServerComPorts.form.keyStorePath', 'MDC', 'Key store file path'),
            name: 'keyStoreFilePath'
        },
        {
            xtype: 'fieldcontainer',
            fieldLabel: Uni.I18n.translate('comServerComPorts.form.keyStorePass', 'MDC', 'Key store access password'),
            required: true,
            name: 'keyStorePassword',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'textfield',
                    required: true,
                    allowBlank: false,
                    inputType: 'password',
                    name: 'keyStorePassword'
                },
                {
                    xtype: 'container',
                    margin: '0 0 8 0',
                    layout: 'hbox',
                    items: [
                        {
                            xtype: 'checkbox',
                            name: 'showChar',
                            margin: '0 16 0 0',
                            listeners: {
                                change: function (chkbx, newValue) {
                                    var passField = chkbx.up('fieldcontainer[name=keyStorePassword]').down('textfield[name=keyStorePassword]');
                                    newValue ? passField.getEl().select('input').elements[0].type = 'text' :
                                        passField.getEl().select('input').elements[0].type = 'password';
                                }
                            }
                        },
                        {
                            xtype: 'label',
                            text: Uni.I18n.translate('comServerComPorts.form.showChar', 'MDC', 'Show characters')
                        }
                    ]
                }
            ]
        },
        {
            xtype: 'textfield',
            required: true,
            allowBlank: false,
            fieldLabel: Uni.I18n.translate('comServerComPorts.form.trustStorePath', 'MDC', 'Trust store file path'),
            name: 'trustStoreFilePath'
        },
        {
            xtype: 'fieldcontainer',
            fieldLabel: Uni.I18n.translate('comServerComPorts.form.trustStorePass', 'MDC', 'Trust store access password'),
            name: 'trustStorePassword',
            required: true,
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'textfield',
                    required: true,
                    allowBlank: false,
                    name: 'trustStorePassword',
                    inputType: 'password'
                },
                {
                    xtype: 'container',
                    margin: '0 0 8 0',
                    allowBlank: false,
                    layout: 'hbox',
                    items: [
                        {
                            xtype: 'checkbox',
                            name: 'showChar',
                            margin: '0 16 0 0',
                            listeners: {
                                change: function (chkbx, newValue) {
                                    var passField = chkbx.up('fieldcontainer[name=trustStorePassword]').down('textfield[name=trustStorePassword]');
                                    newValue ? passField.getEl().select('input').elements[0].type = 'text' :
                                        passField.getEl().select('input').elements[0].type = 'password';
                                }
                            }
                        },
                        {
                            xtype: 'label',
                            text: Uni.I18n.translate('comServerComPorts.form.showChar', 'MDC', 'Show characters')
                        }
                    ]
                }
            ]
        }
    ],

    showInbound: function () {
    },
    showOutbound: function () {
    }
});

