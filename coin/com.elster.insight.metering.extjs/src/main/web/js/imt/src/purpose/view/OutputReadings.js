Ext.define('Imt.purpose.view.OutputReadings', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.output-readings',
    itemId: 'output-readings',
    requires: [
        'Uni.grid.FilterPanelTop',
        'Imt.purpose.view.ReadingsGraph',
        'Imt.purpose.view.ReadingsList'
    ],

    initComponent: function () {
        var me = this,
            output = me.output,
            interval = me.interval,
            all = interval.get('all'),
            duration = all.count + all.timeUnit,
            durations = Ext.create('Uni.store.Durations');

        durations.loadData(interval.get('duration'));
        me.items = [
            {
                xtype: 'uni-grid-filterpaneltop',
                itemId: 'output-readings-topfilter',
                store: 'Imt.purpose.store.Readings',
                hasDefaultFilters: true,
                filters: [
                    {
                        type: 'duration',
                        dataIndex: 'interval',
                        dataIndexFrom: 'intervalStart',
                        dataIndexTo: 'intervalEnd',
                        defaultFromDate: interval.getIntervalStart(output.get('lastReading') || new Date()),
                        defaultDuration: duration,
                        text: Uni.I18n.translate('general.startDate', 'MDC', 'Start date'),
                        durationStore: durations,
                        loadStore: false,
                        //hideDateTtimeSelect: me.filterDefault.hideDateTtimeSelect,
                        itemId: 'devicechannels-topfilter-duration'
                    }
                ]
            },
            {
                xtype: 'readings-graph',
                output: me.output,
                interval: me.interval
            },
            {
                xtype: 'emptygridcontainer',
                grid: {
                    xtype: 'readings-list',
                    router: me.router
                },
                emptyComponent: {
                    xtype: 'no-items-found-panel',
                    itemId: 'readings-empty-panel',
                    title: Uni.I18n.translate('readings.list.empty', 'IMT', 'No data is available'),
                    reasons: [
                        Uni.I18n.translate('readings.list.reason1', 'IMT', 'No data has been collected yet'),
                        Uni.I18n.translate('readings.list.reason2', 'IMT', 'Filter is too narrow')
                    ]
                }
            }
        ];

        me.callParent(arguments);
    }
});