/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.customattributesonvaluesobjects.view.CustomAttributeSetVersionsOnRegister', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.register-history-custom-attribute-sets-versions',

    requires: [
        'Mdc.customattributesonvaluesobjects.view.CustomAttributeSetVersionsSetup',
        'Mdc.view.setup.device.DeviceMenu'
    ],

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'stepsMenu',
                        device: me.device,
                        toggleId: 'registersLink'
                    }
                ]
            }
        ];

        me.content = [
            {
                title: me.router.getRoute().getTitle(),
                ui: 'large',
                type: 'register',
                xtype: 'custom-attribute-set-versions-setup',
                store: 'Mdc.customattributesonvaluesobjects.store.CustomAttributeSetVersionsOnRegister'
            }
        ];

        me.callParent(arguments);
    }
});

