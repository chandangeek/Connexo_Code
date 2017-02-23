/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.register.RegisterMappingPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.registerMappingPreview',
    itemId: 'registerMappingPreview',

    requires: [
        'Mdc.model.RegisterType',
        'Mdc.view.setup.register.RegisterMappingActionMenu',
        'Uni.form.field.ObisDisplay',
        'Uni.form.field.ReadingTypeDisplay',
        'Uni.form.field.CustomAttributeSetDisplay'
    ],

    title: Uni.I18n.translate('general.details','MDC','Details'),

    tools: [
        {
            xtype: 'uni-button-action',
            privileges: Mdc.privileges.DeviceType.admin,
            menu: {
                xtype: 'register-mapping-action-menu',
                itemId: 'register-mapping-action-menu'
            }
        }
    ],

    deviceTypeId: null,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'form',
                border: false,
                itemId: 'registerMappingPreviewForm',
                layout: 'column',
                defaults: {
                    columnWidth: 0.5,
                    layout: 'form'
                },
                items: [
                    {
                        defaults: {
                            xtype: 'displayfield',
                            labelWidth: 200
                        },
                        items: [
                            {
                                xtype: 'reading-type-displayfield',
                                name: 'readingType'
                            },
                            {
                                fieldLabel: Uni.I18n.translate('deviceloadprofiles.customattributeset', 'MDC', 'Custom attribute set'),
                                itemId: 'custom-attribute-set-displayfield-id',
                                xtype: 'custom-attribute-set-displayfield',
                                name: 'customPropertySet'
                            }
                        ]
                    },
                    {
                        defaults: {
                            xtype: 'displayfield',
                            labelWidth: 200
                        },
                        items: [
                            {
                                xtype: 'obis-displayfield',
                                name: 'obisCode'
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});
