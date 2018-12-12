/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.devicetypecustomattributes.view.AddAttributeSetsSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-type-add-custom-attribute-sets-setup',
    itemId: 'device-type-add-custom-attribute-sets-setup-id',

    requires: [
        'Mdc.view.setup.devicetype.SideMenu',
        'Mdc.devicetypecustomattributes.view.AddAttributeSetsGrid'
    ],


    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceTypeSideMenu',
                        itemId: 'stepsMenu',
                        deviceTypeId: me.deviceTypeId
                    }
                ]
            }
        ];

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('general.customAttributeSetsAdd', 'MDC', 'Add custom attribute sets'),
                items: [
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'device-type-add-custom-attribute-sets-grid',
                            itemId: 'device-type-add-custom-attribute-sets-grid-id'
                        },
                        emptyComponent: {
                            xtype: 'container',
                            margin: '0 0 10 0',
                            items: [
                                {
                                    xtype: 'no-items-found-panel',
                                    title: Uni.I18n.translate('customattributesets.noItems', 'MDC', 'No custom attribute sets found'),
                                    reasons: [
                                        Uni.I18n.translate('customattributesets.empty.list.item4', 'MDC', 'All custom attribute sets already added.'),
                                        Uni.I18n.translate('customattributesets.empty.list.item1', 'MDC', 'No custom attribute sets defined yet.')
                                    ]
                                }
                            ]
                        }
                    }
                ]
            },
            {
                xtype: 'container',
                layout: {
                    type: 'hbox'
                },
                items: [
                    {
                        text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                        itemId: 'add-custom-attribute-sets-grid-add',
                        xtype: 'button',
                        ui: 'action',
                        disabled: true
                    },
                    {
                        text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                        itemId: 'add-custom-attribute-sets-grid-cancel',
                        xtype: 'button',
                        ui: 'link'
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});

