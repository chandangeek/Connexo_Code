Ext.define('Imt.channeldata.view.ChannelTopFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'imt-channeldata-topfilter',
    store: 'Imt.channeldata.store.ChannelData',
    filterDefault: {},

    initComponent: function() {
        var me = this;

        this.filters = [
            {
                type: 'duration',
                dataIndex: 'interval',
                dataIndexFrom: 'intervalStart',
                dataIndexTo: 'intervalEnd',
                defaultFromDate: me.filterDefault.fromDate,
                defaultDuration: me.filterDefault.duration,
                text: Uni.I18n.translate('channels.topfilter.startedDate', 'IMT', 'Start date'),
                durationStore: me.filterDefault.durationStore,
                loadStore: false
            }
        ];

        me.callParent(arguments);
    }
});