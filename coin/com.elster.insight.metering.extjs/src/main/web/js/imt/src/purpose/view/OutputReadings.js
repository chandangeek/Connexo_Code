/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
        'Imt.purpose.view.registers.RegisterDataGrid'
    ],
    store: null,
    mixins: {
        bindable: 'Ext.util.Bindable',
        graphWithGrid: 'Uni.util.GraphWithGrid',
        readingsGraph: 'Uni.util.ReadingsGraph'
    },
    idProperty: 'interval_end',

    initComponent: function () {
        var me = this,
            output = me.output,
            emptyComponent,
            durations,
            all,
            duration;

        emptyComponent = {
            xtype: 'no-readings-found-panel',
            itemId: 'readings-empty-panel'
        };

        if (output.get('outputType') === 'register') {
            emptyComponent.stepItems = [
                {
                    text: Uni.I18n.translate('register-data.list.add', 'IMT', 'Add reading'),
                    privileges: Imt.privileges.UsagePoint.admin,
                    href: me.router.getRoute('usagepoints/view/purpose/output/addregisterdata').buildUrl(),
                    action: 'add',
                    itemId: 'add-register-data'
                }
            ];
        }

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
                store: me.store,
                hasDefaultFilters: true,
                filters: [
                    Ext.apply({
                        type: 'duration',
                        dataIndex: 'interval',
                        dataIndexFrom: 'intervalStart',
                        dataIndexTo: 'intervalEnd',
                        text: Uni.I18n.translate('general.startDate', 'IMT', 'Start date'),
                        loadStore: false,
                        itemId: 'output-readings-topfilter-duration'
                    }, duration),
                    {
                        type: 'checkbox',
                        dataIndex: 'suspect',
                        itemId: 'output-readings-topfilter-validation-result',
                        layout: 'hbox',
                        defaults: {margin: '0 10 0 0'},
                        options: [
                            {
                                display: Uni.I18n.translate('reading.validationResult.suspect', 'IMT', 'Suspect'),
                                value: 'suspect',
                                itemId: 'output-readings-topfilter-suspect'
                            }
                        ]
                    }
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
                        interval: me.interval,
                        listeners: {
                            barselect: Ext.bind(me.onBarSelect, me)
                        }
                    },
                    {
                        xtype: 'preview-container',
                        itemId: 'output-readings-preview-container',
                        grid: {
                            xtype: 'readings-list',
                            output: me.output,
                            router: me.router,
                            listeners: {
                                select: function (grid, record) {
                                    me.down('#output-readings-preview-container').fireEvent('rowselect', record);
                                },
                                itemclick: function (dataView, record) {
                                    if (me.down('readings-list').getSelectionModel().isSelected(record)) {
                                        me.down('#output-readings-preview-container').fireEvent('rowselect', record);
                                    }
                                }
                            }
                        },
                        emptyComponent: emptyComponent,
                        previewComponent: {
                            xtype: 'reading-preview',
                            itemId: 'reading-preview',
                            output: me.output,
                            router: me.router,
                            hidden: true,
                            outputType: output.get('outputType')
                        },
                        listeners: {
                            rowselect: Ext.bind(me.onRowSelect, me)
                        }
                    }
                );
                break;
            case 'register':
                me.items.push({
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'register-data-grid',
                        router: me.router,
                        output: me.output
                    },
                    emptyComponent: emptyComponent,
                    previewComponent: {
                        xtype: 'reading-preview',
                        itemId: 'reading-preview',
                        output: me.output,
                        router: me.router,
                        hidden: true,
                        outputType: output.get('outputType')
                    }
                });
                break;
        }

        me.callParent(arguments);
        me.bindStore(me.store || 'ext-empty-store', true);
        me.on('beforedestroy', me.onBeforeDestroy, me);
    },

    getStoreListeners: function () {
        return {
            beforeload: this.onBeforeLoad,
            load: this.onLoad
        };
    },

    onBeforeLoad: function () {
        this.setLoading(true);
    },

    onLoad: function () {
        var me =this;
        if(me.output.get('outputType') == 'channel'){
            this.showGraphView();
        }
        this.setLoading(false);
    },

    onBeforeDestroy: function () {
        this.bindStore('ext-empty-store');
    },

    formatData: function () {
        var me = this,
            data = [],
            missedValues = [],
            output = me.output,
            unitOfMeasure = output.get('readingType').names.unitOfMeasure,
            okColor = "#70BB51",
            estimatedColor = '#568343',
            suspectColor = 'rgba(235, 86, 66, 1)',
            informativeColor = "#dedc49",
            notValidatedColor = "#71adc7",
            tooltipOkColor = 'rgba(255, 255, 255, 0.85)',
            tooltipEstimatedColor = 'rgba(86, 131, 67, 0.3)',
            tooltipSuspectColor = 'rgba(235, 86, 66, 0.3)',
            tooltipInformativeColor = 'rgba(222, 220, 73, 0.3)',
            tooltipNotValidatedColor = 'rgba(0, 131, 200, 0.3)';

        me.store.each(function (record) {
            var point = {},
                interval = record.get('interval'),
                properties = record.get('readingProperties');

            point.x = interval.start;
            point.id = point.x;
            point.y = parseFloat(record.get('value')) || null;
            point.intervalEnd = interval.end;
            point.value = record.get('value');
            point.unitOfMeasure = unitOfMeasure;
            point.color = okColor;
            point.tooltipColor = tooltipOkColor;
            point.showQualityIcon = !Ext.isEmpty(record.get('readingQualities'));

            point.validationRules = record.get('validationRules');

            if (record.get('modificationFlag')) {
                point.edited = true;
            }
            if (record.get('estimatedByRule')) {
                point.color = estimatedColor;
                point.tooltipColor = tooltipEstimatedColor;
            } else if (properties.notValidated) {
                point.color = notValidatedColor;
                point.tooltipColor = tooltipNotValidatedColor
            } else if (properties.suspect) {
                point.color = suspectColor;
                point.tooltipColor = tooltipSuspectColor
            } else if (properties.informative) {
                point.color = informativeColor;
                point.tooltipColor = tooltipInformativeColor;
            }

            Ext.merge(point, properties);
            data.unshift(point);

            !point.y && (point.y = null);
            if (!point.y) {
                if (properties.suspect) {
                    missedValues.push({
                        id: record.get('interval').start,
                        from: record.get('interval').start,
                        to: record.get('interval').end,
                        color: 'rgba(235, 86, 66, 0.3)'
                    });
                    record.set('plotBand', true);
                }
            }
        });

        return {data: data, missedValues: missedValues};
    },

    getValueFromPoint: function (point) {
        return new Date(point.intervalEnd);
    }
});