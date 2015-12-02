Ext.define('Imt.view.setup.devicechannels.EditCustomAttributes', {
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
                        xtype: 'usage-point-management-side-menu',
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
                            text: Uni.I18n.translate('general.save', 'IMT', 'Save')
                        },
                        {
                            xtype: 'button',
                            text: Uni.I18n.translate('general.restoretodefaults', 'IMT', 'Restore to defaults'),
                            icon: '../sky/build/resources/images/form/restore.png',
                            itemId: 'channelCustomAttributesRestoreBtn'
                        },
                        {
                            xtype: 'button',
                            ui: 'link',
                            itemId: 'channelCustomAttributesCancelBtn',
                            text: Uni.I18n.translate('general.cancel', 'IMT', 'Cancel')
                        }
                    ]
                }
            }
        ];

        me.callParent(arguments);
    }
});


