/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicechannels.EditCustomAttributes', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceLoadProfileChannelsEditCustomAttributes',
    itemId: 'deviceLoadProfileChannelsEditCustomAttributes',
    device: null,

    requires: [],

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
                xtype: 'panel',
                itemId: 'channelEditPanel',
                ui: 'large',
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'property-form',
                        itemId: 'channel-custom-attributes-property-form',
                        width: '100%'
                    }
                ],
                dockedItems: {
                    xtype: 'container',
                    dock: 'bottom',
                    margin: '20 0 0 265',
                    layout: {
                        type: 'hbox',
                        align: 'stretch'
                    },

                    items: [
                        {
                            xtype: 'button',
                            itemId: 'channelCustomAttributesSaveBtn',

                            ui: 'action',
                            text: Uni.I18n.translate('general.save', 'MDC', 'Save')
                        },
                        {
                            xtype: 'button',
                            text: Uni.I18n.translate('general.restoretodefaults', 'MDC', 'Restore to defaults'),
                            iconCls: 'icon-rotate-ccw3',
                            itemId: 'channelCustomAttributesRestoreBtn'
                        },
                        {
                            xtype: 'button',
                            ui: 'link',
                            itemId: 'channelCustomAttributesCancelBtn',
                            text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel')
                        }
                    ]
                }
            }
        ];

        me.callParent(arguments);
    }
});


