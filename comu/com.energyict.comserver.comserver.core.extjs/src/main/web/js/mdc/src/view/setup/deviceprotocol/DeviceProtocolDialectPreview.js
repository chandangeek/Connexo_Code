/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceprotocol.DeviceProtocolDialectPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceProtocolDialectPreview',
    itemId: 'deviceProtocolDialectPreview',
    requires: [
        'Mdc.model.ProtocolDialect',
        'Uni.property.form.Property',
        'Mdc.view.setup.deviceprotocol.DeviceProtocolDialectActionMenu'
    ],
    frame: true,
    title: Uni.I18n.translate('general.details','MDC','Details'),

    tools: [
        {
            xtype: 'uni-button-action',
            privileges: Mdc.privileges.Device.administrateDeviceCommunication,
            dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.protocolDialectsActions,
            menu: {
                xtype: 'device-protocol-dialect-action-menu'
            }
        }
    ],

    layout: {
        type: 'card',
        align: 'stretch'
    },


    items: [
        {
            xtype: 'panel',
            border: false,
            tbar: [
                {
                    xtype: 'component',
                    html: '<h4>' + Uni.I18n.translate('protocolDialect.noProtocolDialectSelected', 'MDC', 'No protocol selected') + '</h4>'
                }
            ],
            items: [
                {
                    xtype: 'component',
                    height: '100px',
                    html: '<h5>' + Uni.I18n.translate('protocolDialect.selectProtocolDialect', 'MDC', 'Select a protocol to see its details') + '</h5>'
                }
            ]

        },
        {
            xtype: 'form',
            itemId: 'deviceProtocolDialectPreviewForm',
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
                        }
                    ]
                },
                {
                    xtype: 'form',
                    border: false,
                    itemId: 'protocolDialectsDetailsTitle',
                    hidden: true,
                    defaults: {
                        labelWidth: 250,
                        labelAlign: 'left'
                    },
                    items: [
                        {
                            xtype: 'displayfield',
                            fieldLabel: Uni.I18n.translate('protocolDialect.protocolDialectDetails', 'MDC', 'Protocol dialect details'),
                            renderer: function() {
                                return ''; // No dash!
                            }
                        }
                    ]
                },
                {
                    xtype: 'property-form',
                    isEdit: false,
                    layout: 'column',
                    defaults: {
                        layout: 'form',
                        resetButtonHidden: true,
                        labelWidth: 250,
                        columnWidth: 0.5
                    }
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});