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
            header: Uni.I18n.translate('channels.title.channel', 'IMT', 'Channel'),
            flex: 1,
            dataIndex: 'readingTypeFullAliasName',
            renderer: function (value, b, record) {
                var me = this,
                    url = me.router.getRoute('usagepoints/view/channels/channel').buildUrl({mRID: me.mRID, channelId: record.get('readingTypemRID')});

                return '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
            }
        },
        {
            header: Uni.I18n.translate('usagepoint.interval', 'IMT', 'Interval'),            
            flex: 1,
            dataIndex: 'interval',
            renderer: function (value) {
                var res = '';
                value ? res = Ext.String.htmlEncode('{count} {timeUnit}'.replace('{count}', value.count).replace('{timeUnit}', value.timeUnit)) : null;
                return res
            }
        }
    ]
});