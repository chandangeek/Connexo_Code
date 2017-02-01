/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicechannels.EditChannel', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Mdc.view.setup.devicechannels.EditChannelForm'
    ],
    alias: 'widget.device-channel-edit',
    device: null,
    returnLink: null,

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
                xtype: 'device-channel-edit-form',
                itemId: 'mdc-device-channel-edit-form',
                title: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                ui: 'large',
                width: '100%',
                defaults: {
                    labelWidth: 250
                },
                returnLink: me.returnLink
            }
        ];
        me.callParent(arguments);
    },

    setChannel: function(channelRecord) {
        var me = this;
        if (me.rendered) {
            me.down('#mdc-device-channel-edit-form').setChannel(channelRecord);
        } else {
            me.on('afterrender', function() {
                me.down('#mdc-device-channel-edit-form').setChannel(channelRecord);
            }, me, {single:true});
        }
    }
});