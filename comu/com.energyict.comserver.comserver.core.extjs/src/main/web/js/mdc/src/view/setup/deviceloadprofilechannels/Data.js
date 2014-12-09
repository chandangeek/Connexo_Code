Ext.define('Mdc.view.setup.deviceloadprofilechannels.Data', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceLoadProfileChannelData',
    itemId: 'deviceLoadProfileChannelData',
    requires: [
        'Mdc.view.setup.deviceloadprofilechannels.TableView',
        'Mdc.view.setup.deviceloadprofilechannels.GraphView',
        'Mdc.view.setup.deviceloadprofilechannels.SideFilter',
        'Uni.view.toolbar.PagingTop'
    ],

    router: null,
    channel: null,

    initComponent: function () {
        var me = this;
        me.content = {
            ui: 'large',
            title: Uni.I18n.translate('deviceloadprofiles.channels.channelData', 'MDC', 'Channel data'),
            tools: [
                {
                    xtype: 'button',
                    itemId: 'deviceLoadProfileChannelTableViewBtn',
                    text: Uni.I18n.translate('deviceloadprofiles.tableView', 'MDC', 'Table view'),
                    action: 'showTableView',
                    ui: 'link'
                },
                {
                    xtype: 'tbtext',
                    text: '|'
                },
                {
                    xtype: 'button',
                    itemId: 'deviceLoadProfileChannelGraphViewBtn',
                    text: Uni.I18n.translate('deviceloadprofiles.graphView', 'MDC', 'Graph view'),
                    action: 'showGraphView',
                    disabled: true,
                    ui: 'link'
                }
            ],
            items: [
                {
                    xtype: 'filter-top-panel',
                    itemId: 'deviceloadprofileschanneldatafilterpanel',
                    emptyText: Uni.I18n.translate('general.none', 'MDC', 'None')
                },
                {
                    items: [

                        {
                            xtype: 'deviceLoadProfileChannelTableView',
                            channel: me.channel,
                            router: me.router
                        },
                        {
                            xtype: 'deviceLoadProfileChannelGraphView'
                        }
                    ]
                }
            ]
        };

        me.side = {
            xtype: 'panel',
            ui: 'medium',
            items: [
                {
                    xtype: 'panel',
                    title: Uni.I18n.translate('deviceregisterconfiguration.devices', 'MDC', 'Devices'),
                    ui: 'medium',
                    items: [
                        {
                            xtype: 'deviceMenu',
                            itemId: 'stepsMenu',
                            device: me.device,
                            toggleId: 'channelsLink',
                            mRID: me.mRID
                        }
                    ]
                },
                {
                    xtype: 'deviceLoadProfileChannelDataSideFilter'
                }
            ]
        };

        me.callParent(arguments);
    }
});

