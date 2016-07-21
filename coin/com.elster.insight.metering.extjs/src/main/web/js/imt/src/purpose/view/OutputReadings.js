Ext.define('Imt.purpose.view.OutputReadings', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.output-readings',
    itemId: 'output-readings',
    requires: [
        'Uni.grid.FilterPanelTop',
        'Imt.purpose.view.ReadingsGraph',
        'Imt.purpose.view.ReadingsList',
        'Imt.purpose.view.ReadingPreview'
        'Imt.purpose.view.NoReadingsFoundPanel',
        'Imt.purpose.view.RegisterDataGrid'
    ],
    dataStore: null,

    initComponent: function () {
        var me = this,
            output = me.output,
            isComplete = me.purpose.get('status').id === 'complete',
            emptyComponent = {
                xtype: 'no-readings-found-panel',
                itemId: 'readings-empty-panel'
            },
            durations,
            all,
            duration;

        if (me.interval) {
            durations = Ext.create('Uni.store.Durations');
            all = me.interval.get('all');
            duration = {
                defaultFromDate: me.interval.getIntervalStart(output.get('lastReading') || new Date()),
                defaultDuration: all.count + all.timeUnit,
                durationStore: durations
            };
            durations.loadData(me.interval.get('duration'));
        } else {
            duration = {
                defaultFromDate: moment().startOf('day').subtract(1, 'years').toDate(),
                defaultDuration: '1years'
            };
        }

        me.items = [
            {
                xtype: 'uni-form-empty-message',
                text: Uni.I18n.translate('readings.list.incomplete', 'IMT', "You can't view readings because purpose is incomplete."),
                hidden: isComplete
            },
            {
                xtype: 'uni-grid-filterpaneltop',
                itemId: 'output-readings-topfilter',
                store: me.dataStore,
                hasDefaultFilters: true,
                hidden: !isComplete,
                filters: [
                    Ext.apply({
                        type: 'duration',
                        dataIndex: 'interval',
                        dataIndexFrom: 'intervalStart',
                        dataIndexTo: 'intervalEnd',
                        text: Uni.I18n.translate('general.startDate', 'IMT', 'Start date'),
                        loadStore: false,
                        itemId: 'devicechannels-topfilter-duration'
                    }, duration)
                ]
            }
        ];

        switch (output.get('outputType')) {
            case 'channel':
                me.items.push({
                        xtype: 'readings-graph',
                        output: me.output,
                        interval: me.interval,
                        hidden: !isComplete
                    },
                    {
                        xtype: 'emptygridcontainer',
                        hidden: !isComplete,
                        grid: {
                            xtype: 'readings-list',
                            output: me.output,
                            router: me.router
                        },
                        emptyComponent: emptyComponent
                    });
                break;
            case 'register':
                me.items.push({
                    xtype: 'emptygridcontainer',
                    hidden: !isComplete,
                    grid: {
                        xtype: 'register-data-grid',
                        output: me.output
                    },
                    emptyComponent: emptyComponent
                });
                break;
        }
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