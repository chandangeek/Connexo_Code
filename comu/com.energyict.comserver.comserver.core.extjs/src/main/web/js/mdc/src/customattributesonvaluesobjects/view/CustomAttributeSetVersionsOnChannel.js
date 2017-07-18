/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.customattributesonvaluesobjects.view.CustomAttributeSetVersionsOnChannel', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.channel-history-custom-attribute-sets-versions',

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
                        toggleId: 'channelsLink'
                    }
                ]
            }
        ];

        me.content = [
            {
                title: me.router.getRoute().getTitle(),
                ui: 'large',
                type: 'channel',
                xtype: 'custom-attribute-set-versions-setup',
                store: 'Mdc.customattributesonvaluesobjects.store.CustomAttributeSetVersionsOnChannel'
            }
        ];

        me.callParent(arguments);
    }
});