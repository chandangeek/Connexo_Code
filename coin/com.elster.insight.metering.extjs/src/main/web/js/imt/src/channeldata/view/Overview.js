Ext.define('Imt.channeldata.view.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.channelOverview',
    itemId: 'channelOverview',

    requires: [
        'Imt.channeldata.view.PreviewForm',
        'Imt.channeldata.view.ValidationOverview',
        'Imt.channeldata.view.ActionMenu'
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
                            xtype: 'channelsPreviewForm',
                            device: me.device,
                            router: me.router,
                            margin: '0 0 0 0',
                            itemId: 'channelsOverviewForm'
                        }
                    },
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.actions', 'IMT', 'Actions'),
                        iconCls: 'x-uni-action-iconD',
                        margin: '20 0 0 0',
                        menu: {
                            xtype: 'channelsActionMenu'
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});