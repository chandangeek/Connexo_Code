/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicecommunicationprotocol.DeviceCommunicationProtocolPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceCommunicationProtocolPreview',
    frame: true,
    border: true,
    itemId: 'deviceCommunicationProtocolPreview',

    requires: [
        'Uni.property.form.Property',
        'Mdc.model.DeviceCommunicationProtocol',
        'Mdc.view.setup.devicecommunicationprotocol.DeviceCommunicationProtocolActionMenu'
    ],

    layout: {
        type: 'card',
        align: 'stretch'
    },

    title: Uni.I18n.translate('deviceCommunicationProtocol.previewTitle', 'MDC', 'Selected protocol preview'),

    tools: [
        {
            xtype: 'uni-button-action',
            privileges: Mdc.privileges.Communication.admin,
            menu: {
                xtype: 'device-communication-protocol-action-menu'
            }
        }
    ],

    initComponent: function () {
        var me = this;
        this.items = [
            {
                xtype: 'panel',
                border: false,
                tbar: [
                    {
                        xtype: 'component',
                        html: '<h4>' + Uni.I18n.translate('deviceCommunicationProtocol.noDeviceCommunicationProtocolSelected', 'MDC', 'No protocol selected') + '</h4>'
                    }
                ],
                items: [
                    {
                        xtype: 'component',
                        html: '<h5>' + Uni.I18n.translate('deviceCommunicationProtocol.selectDeviceCommunicationProtocol', 'MDC', 'Select a protocol to see its details') + '</h5>'
                    }
                ]
            },
            {
                xtype: 'form',
                border: false,
                itemId: 'deviceCommunicationProtocolPreviewForm',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'container',
                        layout: {
                            type: 'hbox',
                            align: 'stretch'
                        },
                        defaults: {
                            labelWidth: 250,
                            flex: 1
                        },
                        items: [
                            {
                                xtype: 'displayfield',
                                name: 'name',
                                fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name')
                            },
                            {
                                xtype: 'displayfield',
                                name: 'deviceProtocolVersion',
                                fieldLabel: Uni.I18n.translate('deviceCommunicationProtocol.version', 'MDC', 'Version')
                            }
                        ]
                    },
                    {
                        xtype: 'component',
                        margins: '10 0 20 0',
                        html: '<h3 style="text-align: left;">' +
                            Uni.I18n.translate('deviceCommunicationProtocol.communicationProtocolDetails', 'MDC', 'Communication protocol details') +
                            '</h3>'
                    },
                    {
                        xtype: 'property-form',
                        isEdit: false,
                        layout: 'column',
                        frame: false,
                        defaults: {
                            layout: 'form',
                            resetButtonHidden: true,
                            labelWidth: 250,
                            columnWidth: 0.5
                        }
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});