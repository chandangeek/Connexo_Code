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
                //TODO: Fix to use router and get mRID from somewhere for the UP
                return '<a href="#/administration/usagepoints/' + record.get('id') + '/channeldata">' + Ext.String.htmlEncode(value) + '</a>';
            }
        }
    ]
});