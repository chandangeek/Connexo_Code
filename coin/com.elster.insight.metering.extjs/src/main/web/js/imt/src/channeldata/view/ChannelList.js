Ext.define('Imt.channeldata.view.ChannelList', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.channel-list',
    requires: [
        'Imt.channeldata.store.Channel'
    ],
    store: 'Imt.channeldata.store.Channel',
    overflowY: 'auto',
    itemId: 'channelList',
    title: Uni.I18n.translate('channels.channelList', 'IMT', 'Channel List'),
    
    columns: [
        {
            header: Uni.I18n.translate('channels.title.channels', 'MDC', 'Channels'),
            flex: 1,
            dataIndex: 'name',
            renderer: function (value, b, record) {
                var me = this;
                //TODO: Fix to use router
                return '<a href="#/administration/usagepoints/' + me.mRID + '/channeldata/' + record.get('id').toString() + '">' + Ext.String.htmlEncode(value) + '</a>';
            }
        }
    ]
});