Ext.define('Imt.channeldata.view.ChannelList', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.channel-list',
    requires: [
        'Imt.channeldata.store.Channel'
    ],
    store: 'Imt.channeldata.store.Channel',
    overflowY: 'auto',
    itemId: 'channelList',
    title: Uni.I18n.translate('channels.channelList', 'IMT', 'Channels'),
    
    columns: [
        {
            header: Uni.I18n.translate('channels.title.channels', 'MDC', 'Channels'),
            flex: 1,
            dataIndex: 'name',
            renderer: function (value, b, record) {
                var me = this,
                    url = me.router.getRoute('administration/usagepoint/channels/channel').buildUrl({mRID: me.mRID, channel: record.get('id')});

                return '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
            }
        }
    ]
});