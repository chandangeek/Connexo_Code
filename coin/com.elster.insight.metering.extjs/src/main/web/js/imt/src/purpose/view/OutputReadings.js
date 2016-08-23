Ext.define('Imt.purpose.view.OutputReadings', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.output-readings',
    itemId: 'output-readings',
    requires: [
        'Uni.grid.FilterPanelTop',
        'Imt.purpose.view.ReadingsGraph',
        'Imt.purpose.view.ReadingsList',
        'Imt.purpose.view.ReadingPreview',
        'Imt.purpose.view.NoReadingsFoundPanel',
        'Imt.purpose.view.RegisterDataGrid'
    ],
    dataStore: null,

    initComponent: function () {
        var me = this,
            output = me.output,
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
                xtype: 'uni-grid-filterpaneltop',
                itemId: 'output-readings-topfilter',
                store: me.dataStore,
                hasDefaultFilters: true,
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
                me.items.push(
                    {
                        xtype: 'readings-graph',
                        router: me.router,
                        output: me.output,
                        interval: me.interval
                    },
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'readings-list',
                            output: me.output,
                            router: me.router
                        },
                        emptyComponent: emptyComponent,
                        previewComponent: {
                            xtype: 'reading-preview',
                            itemId: 'reading-preview',
                            output: me.output,
                            router: me.router,
                            hidden: true
                        }
                    }
                );
                break;
            case 'register':
                me.items.push({
                    xtype: 'emptygridcontainer',
                    grid: {
                        xtype: 'register-data-grid',
                        output: me.output
                    },
                    emptyComponent: emptyComponent
                });
                break;
        }

        me.callParent(arguments);
    }
});