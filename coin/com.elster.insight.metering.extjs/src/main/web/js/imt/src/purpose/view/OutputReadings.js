Ext.define('Imt.purpose.view.OutputReadings', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.output-readings',
    itemId: 'output-readings',
    requires: [
        'Uni.grid.FilterPanelTop',
        'Imt.purpose.view.ReadingsGraph',
        'Imt.purpose.view.ReadingsList',
        'Imt.purpose.view.ReadingPreview'
    ],

    initComponent: function () {
        var me = this,
            output = me.output,
            interval = me.interval,
            all = interval.get('all'),
            duration = all.count + all.timeUnit,
            durations = Ext.create('Uni.store.Durations'),
            isComplete = me.purpose.get('status').id === 'complete';

        durations.loadData(interval.get('duration'));
        me.items = [
            {
                xtype: 'uni-form-empty-message',
                text: Uni.I18n.translate('readings.list.incomplete', 'IMT', "You can't view readings because purpose is incomplete."),
                hidden: isComplete
            },
            {
                xtype: 'uni-grid-filterpaneltop',
                itemId: 'output-readings-topfilter',
                store: 'Imt.purpose.store.Readings',
                hasDefaultFilters: true,
                hidden: !isComplete,
                filters: [
                    {
                        type: 'duration',
                        dataIndex: 'interval',
                        dataIndexFrom: 'intervalStart',
                        dataIndexTo: 'intervalEnd',
                        defaultFromDate: interval.getIntervalStart(output.get('lastReading') || new Date()),
                        defaultDuration: duration,
                        text: Uni.I18n.translate('general.startDate', 'IMT', 'Start date'),
                        durationStore: durations,
                        loadStore: false,
                        //hideDateTtimeSelect: me.filterDefault.hideDateTtimeSelect,
                        itemId: 'devicechannels-topfilter-duration'
                    }
                ]
            },
            {
                xtype: 'readings-graph',
                router: me.router,
                output: me.output,
                interval: me.interval,
                hidden: !isComplete
            },
            {
                xtype: 'preview-container',
                hidden: !isComplete,
                grid: {
                    xtype: 'readings-list',
                    output: me.output,
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
                },
                previewComponent: {
                    xtype: 'reading-preview',
                    itemId: 'reading-preview',
                    output: me.output,
                    router: me.router,
                    hidden: true
                }
            }
        ];

        me.callParent(arguments);
    }
});