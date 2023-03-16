/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.licensedprotocol.Detail', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.licensedProtocolDetail',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    autoShow: true,
    border: 0,
    autoWidth: true,
    requires: [
        'Mdc.view.setup.protocolfamily.List'
    ],
    initComponent: function () {
        this.items = [
            {
                xtype: 'form',
                shrinkWrap: 1,
                padding: 10,
                border: 0,
                defaults: {
                    labelWidth: 200
                },
                items: [
                    {
                        xtype: 'fieldset',
                        title: Uni.I18n.translate('protocol.licansedProtocolInfo','MDC','Licensed Protocol Info'),
                        defaults: {
                            labelWidth: 200
                        },
                        collapsible: true,
                        layout: 'anchor',
                        items: [
                            {
                                xtype: 'textfield',
                                name: 'protocolJavaClassName',
                                fieldLabel: 'Java Class Name',
                                id: 'protocolJavaClassName',
                                readOnly: true,
                                autoWidth: true,
                                size: 50,
                                vtype: 'checkForBlacklistCharacters'
                            },
                            {
                                xtype: 'textfield',
                                name: 'deviceProtocolVersion',
                                fieldLabel: 'Version',
                                autoWidth: true,
                                readOnly: true,
                                size: 50,
                                vtype: 'checkForBlacklistCharacters'
                            },
                            {"xtype": 'setupProtocolFamilies'}
                        ]
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
})
;