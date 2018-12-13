/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.protocoldialect.ProtocolDialectPreview', {
    extend: 'Ext.form.Panel',
    alias: 'widget.protocolDialectPreview',
    itemId: 'protocolDialectPreview',
    requires: [
        'Mdc.model.ProtocolDialect',
        'Uni.property.form.Property',
        'Uni.button.Action',
        'Mdc.view.setup.protocoldialect.ProtocolDialectActionMenu'
    ],
    frame: true,
    title: '',

    tools: [
        {
            xtype: 'uni-button-action',
            privileges: Mdc.privileges.DeviceType.admin,
            menu: {
                xtype: 'protocol-dialect-action-menu'
            }
        }
    ],

    items: [
            {
            xtype: 'form',
            itemId: 'protocolDialectPreviewForm',
            defaults: {
                labelWidth: 250
            },
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'displayfield',
                    name: 'name',
                    fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name')
                },
                {
                    itemId: 'protocolDialectsDetailsTitle',
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('protocolDialect.protocolDialectDetails', 'MDC', 'Protocol dialect details'),
                    renderer: function() {
                        return ''; // No dash!
                    }
                },
                {
                    xtype: 'property-form',
                    isEdit: false,
                    layout: 'column',

                    defaults: {
                        xtype: 'displayfield',
                        layout: 'vbox',
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



