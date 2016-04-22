Ext.define('Imt.purpose.view.OutputReadings', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.output-readings',
    itemId: 'output-readings',
    requires: [
        'Uni.grid.FilterPanelTop',
        'Imt.purpose.view.ReadingsList'
        //'Uni.view.toolbar.PagingTop',
        //'Uni.grid.column.ReadingType'
    ],
    //store: 'Imt.purpose.store.Outputs',
    //overflowY: 'auto',

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
                xtype: 'emptygridcontainer',
                grid: {
                    xtype: 'readings-list',
                    router: me.router
                },
                emptyComponent: {
                    xtype: 'no-items-found-panel',
                    itemId: 'ctr-no-metrology-configurations',
                    title: Uni.I18n.translate('outputs.list.empty', 'IMT', 'No outputs is configured for selected purpose'),
                    //reasons: [
                    //    Uni.I18n.translate('metrologyconfiguration.list.undefined', 'IMT', 'No metrology configurations have been defined yet.')
                    //],
                    stepItems: [
                        {
                            text: Uni.I18n.translate('outputs.actions.manage', 'IMT', 'Manage outputs'),
//                                privileges : Cfg.privileges.Validation.admin,
                            href: '#' //TODO: future functionality
                        }
                    ]
                }
            }
        ];

        me.callParent(arguments);
    }
});