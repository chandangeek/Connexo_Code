Ext.define('Mdc.view.setup.devicechannels.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceLoadProfileChannelOverview',
    itemId: 'deviceLoadProfileChannelOverview',

    requires: [
        'Mdc.view.setup.devicechannels.PreviewForm',
        'Mdc.view.setup.devicechannels.ValidationOverview',
        'Mdc.view.setup.devicechannels.ActionMenu'
    ],

    router: null,
    device: null,
    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'container',
                layout: 'hbox',
                items: [
                    {
                        ui: 'large',
                        flex: 1,
                        items: {
                            xtype: 'deviceLoadProfileChannelsPreviewForm',
                            device: me.device,
                            router: me.router,
                            margin: '0 0 0 100',
                            itemId: 'deviceLoadProfileChannelsOverviewForm'
                        }
                    },
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
                        iconCls: 'x-uni-action-iconD',
                        margin: '20 0 0 0',
                        menu: {
                            xtype: 'deviceLoadProfileChannelsActionMenu'
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});