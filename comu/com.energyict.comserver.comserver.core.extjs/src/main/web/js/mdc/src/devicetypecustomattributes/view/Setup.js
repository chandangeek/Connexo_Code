/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.devicetypecustomattributes.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-type-custom-attribute-sets-setup',
    itemId: 'device-type-custom-attribute-sets-setup-id',

    requires: [
        'Mdc.view.setup.devicetype.SideMenu',
        'Mdc.devicetypecustomattributes.view.AttributeSetsGrid'
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
                title: Uni.I18n.translate('general.customAttributeSets', 'MDC', 'Custom attribute sets'),
                items: [
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'device-type-custom-attribute-sets-grid',
                            itemId: 'device-type-custom-attribute-sets-grid-id'
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            title: Uni.I18n.translate('customattributesets.noItems', 'MDC', 'No custom attribute sets found'),
                            reasons: [
                                Uni.I18n.translate('customattributesets.empty.list.item3', 'MDC', 'No custom attribute sets added yet.'),
                                Uni.I18n.translate('customattributesets.empty.list.item1', 'MDC', 'No custom attribute sets defined yet.')
                            ],
                            stepItems: [
                                {
                                    text: Uni.I18n.translate('customattributesets.addattributesets', 'MDC', 'Add custom attribute sets'),
                                    itemId: 'device-type-custom-attribute-sets-steps-add-button',
                                    privileges: Mdc.privileges.DeviceType.admin,
                                    action: 'addAttributeSets'
                                }
                            ]
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});
