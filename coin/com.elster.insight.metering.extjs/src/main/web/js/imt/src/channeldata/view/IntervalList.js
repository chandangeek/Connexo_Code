Ext.define('Imt.channeldata.view.IntervalList', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.interval-list',
    requires: [
        'Imt.channeldata.store.Channel'
    ],
    height: 600,
    store: 'Imt.channeldata.store.Channel',
    itemId: 'intervalList',
    
    columns: [
        {
            header: Uni.I18n.translate('channels.title.intervalEnd', 'IMT', 'Edn of Interval'),
            flex: 1,
            dataIndex: 'interval_end'
        },
        {
            header: Uni.I18n.translate('channels.title..intervalValue', 'IMT', 'Value'),            
            flex: 1,
            dataIndex: 'value'
        }
    ]
});