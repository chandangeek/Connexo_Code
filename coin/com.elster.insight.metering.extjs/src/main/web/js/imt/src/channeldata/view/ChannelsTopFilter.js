Ext.define('Imt.channeldata.view.ChannelsTopFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'channelsTopFilter',

    store: 'Imt.channeldata.store.Channels',

    filters: [
        {
            type: 'text',
            dataIndex: 'channelName',
            emptyText: Uni.I18n.translate('devicechannels.channelstopfilter.channelname.emptytext', 'IMT', 'Channel name')
        }
    ]
});