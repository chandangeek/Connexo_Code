Ext.define('Mdc.view.setup.devicechannels.TabbedDeviceChannelsView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.tabbedDeviceChannelsView',
    itemId: 'tabbedDeviceChannelsView',
    requires: [
        'Uni.view.toolbar.PreviousNextNavigation',
        'Mdc.view.setup.devicechannels.TableView',
        'Mdc.view.setup.devicechannels.GraphView',
        'Uni.grid.FilterPanelTop'
    ],

    store: 'Mdc.store.ChannelOfLoadProfileOfDeviceData',

    mixins: {
        bindable: 'Ext.util.Bindable'
    },

    prevNextstore: null,
    routerIdArgument: null,
    isFullTotalCount: false,

    router: null,
    channel: null,
    device: null,
    prevNextListLink: null,
    activeTab: null,
    indexLocation: null,
    contentName: null,
    filterDefault: {},
    mentionDataLoggerSlave: false,

    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'tabpanel',
                ui: 'large',
                title: me.title,
                itemId: 'channelTabPanel',
                activeTab: me.activeTab,
                items: [
                    {
                        title: Uni.I18n.translate('deviceloadprofiles.specifications', 'MDC', 'Specifications'),
                        itemId: 'channel-specifications',
                        items: {
                            xtype: 'deviceLoadProfileChannelOverview',
                            router: me.router,
                            device: me.device
                        }
                    },
                    {
                        title: Uni.I18n.translate('deviceloadprofiles.readings', 'MDC', 'Readings'),
                        itemId: 'deviceLoadProfileChannelData',
                        items: [
                            {
                                xtype: 'uni-grid-filterpaneltop',
                                itemId: 'mdc-device-channels-topfilter',
                                store: 'Mdc.store.ChannelOfLoadProfileOfDeviceData',
                                hasDefaultFilters: true,
                                filters: [
                                    {
                                        type: 'duration',
                                        dataIndex: 'interval',
                                        dataIndexFrom: 'intervalStart',
                                        dataIndexTo: 'intervalEnd',
                                        defaultFromDate: me.filterDefault.fromDate,
                                        defaultDuration: me.filterDefault.duration,
                                        text: Uni.I18n.translate('general.startDate', 'MDC', 'Start date'),
                                        durationStore: me.filterDefault.durationStore,
                                        loadStore: false,
                                        hideDateTtimeSelect: me.filterDefault.hideDateTtimeSelect,
                                        itemId: 'devicechannels-topfilter-duration'
                                    },
                                    {
                                        type: 'checkbox',
                                        dataIndex: 'suspect',
                                        layout: 'hbox',
                                        defaults: {margin: '0 10 0 0'},
                                        emptyText: Uni.I18n.translate('communications.widget.topfilter.validationResult', 'MDC', 'Validation result'),
                                        options: me.filterDefault.options,
                                        itemId: 'devicechannels-topfilter-checkbox'
                                    }
                                ]
                            },
                            {
                                xtype: 'deviceLoadProfileChannelGraphView',
                                mentionDataLoggerSlave : me.mentionDataLoggerSlave
                            },
                            {
                                xtype: 'deviceLoadProfileChannelTableView',
                                channel: me.channel,
                                router: me.router,
                                mentionDataLoggerSlave: !Ext.isEmpty(me.device) && !Ext.isEmpty(me.device.get('isDataLogger')) && me.device.get('isDataLogger')
                            }
                        ]
                    }
                ],
                listeners: {
                    afterrender: function (panel) {
                        var bar = panel.tabBar;
                        bar.insert(2, [
                            {
                                xtype: 'tbfill'
                            },
                            {
                                xtype: 'previous-next-navigation-toolbar',
                                itemId: 'tabbed-device-channels-view-previous-next-navigation-toolbar',
                                store: me.prevNextstore,
                                router: me.router,
                                routerIdArgument: me.routerIdArgument,
                                itemsName: me.prevNextListLink,
                                indexLocation: me.indexLocation,
                                isFullTotalCount: me.isFullTotalCount
                            }
                        ]);
                    }
                }
            }
        ];
        me.side = [
            {
                xtype: 'panel',
                itemId: 'sideChannelsPanel',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [
                    {
                        ui: 'medium',
                        items: [
                            {
                                xtype: 'deviceMenu',
                                itemId: 'stepsMenu',
                                device: me.device,
                                channelId: me.channelId,
                                toggleId: 'channelsLink'
                            }
                        ]
                    }
                ]
            }

        ];
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
        this.showGraphView();
        this.setLoading(false);
    },

    onBeforeDestroy: function () {
        this.bindStore('ext-empty-store');
    },

    showGraphView: function () {
        var me = this,
            dataStore = me.store,
            channelRecord = me.channel,
            container = me.down('deviceLoadProfileChannelGraphView'),
            zoomLevelsStore = Ext.getStore('Uni.store.DataIntervalAndZoomLevels'),
            calculatedReadingType = channelRecord.get('calculatedReadingType'),
            channelName = calculatedReadingType && calculatedReadingType.fullAliasName ? calculatedReadingType.fullAliasName : '',
            unitOfMeasure = channelRecord.get('readingType').unit,
            seriesObject = {
                marker: {
                    enabled: false
                },
                name: channelName
            },
            yAxis = {
                opposite: false,
                gridLineDashStyle: 'Dot',
                showEmpty: false,
                title: {
                    rotation: 270,
                    text: unitOfMeasure
                }
            },
            series = [],
            intervalRecord,
            zoomLevels,
            intervalLengthInMs;

        seriesObject['data'] = [];

        intervalRecord = zoomLevelsStore.getIntervalRecord(channelRecord.get('interval'));
        intervalLengthInMs = zoomLevelsStore.getIntervalInMs(channelRecord.get('interval'));
        zoomLevels = intervalRecord.get('zoomLevels');

        switch (channelRecord.get('flowUnit')) {
            case 'flow':
                seriesObject['type'] = 'line';
                seriesObject['step'] = false;
                break;
            case 'volume':
                seriesObject['type'] = 'column';
                seriesObject['step'] = true;
                break;
        }

        Ext.suspendLayouts();
        if (dataStore.getTotalCount() > 0) {
            var data = me.formatData();
            seriesObject['data'] = data.data;
            seriesObject['turboThreshold'] = Number.MAX_VALUE;

            series.push(seriesObject);
            container.down('#graphContainer').show();
            container.drawGraph(yAxis, series, intervalLengthInMs, channelName, unitOfMeasure, zoomLevels, data.missedValues);
        } else {
            container.down('#graphContainer').hide();
        }
        me.updateLayout();
        Ext.resumeLayouts(true);
    },

    formatData: function () {
        var me = this,
            data = [],
            missedValues = [],
            collectedUnitOfMeasure = me.channel.get('readingType').names.unitOfMeasure,
            calculatedUnitOfMeasure = me.channel.get('calculatedReadingType') ? me.channel.get('calculatedReadingType').names.unitOfMeasure : collectedUnitOfMeasure,
            okColor = '#70BB51',
            estimatedColor = '#568343',
            suspectColor = 'rgba(235, 86, 66, 1)',
            informativeColor = '#dedc49',
            notValidatedColor = '#71adc7',
            dataSlaveColor = '#d2d2d2',
            tooltipOkColor = 'rgba(255, 255, 255, 0.85)',
            tooltipSuspectColor = 'rgba(235, 86, 66, 0.3)',
            tooltipEstimatedColor = 'rgba(86, 131, 67, 0.3)',
            tooltipInformativeColor = 'rgba(222, 220, 73, 0.3)',
            tooltipNotValidatedColor = 'rgba(0, 131, 200, 0.3)',
            tooltipDataSlaveColor = 'rgba(210, 210, 210, 0.3)';

        me.store.each(function (record) {
            var point = {},
                interval = record.get('interval'),
                mainValidationInfo = record.get('mainValidationInfo'),
                bulkValidationInfo = record.get('bulkValidationInfo'),
                properties = record.get('readingProperties'),
                slaveChannelInfo = record.get('slaveChannel');

            point.x = interval.start;
            point.id = point.x;
            point.y = parseFloat(record.get('value'));
            point.yValueFormatted = point.y ? Uni.Number.formatNumber(
                record.get('value').toString(),
                this.channel && !Ext.isEmpty(this.channel.get('overruledNbrOfFractionDigits')) ? this.channel.get('overruledNbrOfFractionDigits') : -1
            ) : '';
            point.intervalEnd = interval.end;
            point.collectedValue = record.get('collectedValue');
            point.collectedValueFormatted = point.collectedValue ? Uni.Number.formatNumber(
                point.collectedValue.toString(),
                this.channel && !Ext.isEmpty(this.channel.get('overruledNbrOfFractionDigits')) ? this.channel.get('overruledNbrOfFractionDigits') : -1
            ) : '';
            point.collectedUnitOfMeasure = collectedUnitOfMeasure;
            point.calculatedUnitOfMeasure = calculatedUnitOfMeasure;
            point.color = okColor;
            point.tooltipColor = tooltipOkColor;
            point.multiplier = record.get('multiplier');

            if (mainValidationInfo.valueModificationFlag == 'EDITED') {
                point.edited = true;
            }
            if (mainValidationInfo.estimatedByRule) {
                point.color = estimatedColor;
                point.tooltipColor = tooltipEstimatedColor;
            } else if (properties.delta.notValidated) {
                point.color = notValidatedColor;
                point.tooltipColor = tooltipNotValidatedColor
            } else if (properties.delta.suspect) {
                point.color = suspectColor;
                point.tooltipColor = tooltipSuspectColor
            } else if (properties.delta.informative) {
                point.color = informativeColor;
                point.tooltipColor = tooltipInformativeColor;
            }

            if (!Ext.isEmpty(slaveChannelInfo)) {
                point.dataLoggerSlave = Ext.String.htmlEncode(slaveChannelInfo.mrid);
                point.color = dataSlaveColor;
                point.tooltipColor = tooltipDataSlaveColor;
            }

            if (bulkValidationInfo.valueModificationFlag == 'EDITED') {
                point.bulkEdited = true;
            }

            Ext.merge(point, properties);
            data.unshift(point);

            !point.y && (point.y = null);
            if (!point.y) {
                if (properties.delta.suspect) {
                    missedValues.push({
                        id: record.get('interval').start,
                        from: record.get('interval').start,
                        to: record.get('interval').end,
                        color: 'rgba(235, 86, 66, 0.3)'
                    });
                    record.set('plotBand', true);
                }
            }
        }, me);
        return {data: data, missedValues: missedValues};
    }
});
