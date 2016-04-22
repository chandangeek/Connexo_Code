Ext.define('Imt.purpose.view.OutputChannelMain', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.output-channel-main',
    itemId: 'tabbedDeviceChannelsView',
    requires: [
        'Uni.view.toolbar.PreviousNextNavigation',
        'Imt.purpose.view.OutputSpecificationsForm',
        'Imt.purpose.view.OutputReadings'
    ],

    initComponent: function () {
        var me = this,
            router= me.router;

        me.content = [
            {
                xtype: 'tabpanel',
                ui: 'large',
                title: router.getRoute().getTitle(),
                itemId: 'channelTabPanel',
                activeTab: me.activeTab,
                items: [
                    {
                        title: Uni.I18n.translate('deviceloadprofiles.specifications', 'MDC', 'Specifications'),
                        itemId: 'channel-specifications',
                        items: {
                            xtype: 'output-specifications-form',
                            router: me.router
                        },
                        listeners: {
                            activate: me.controller.showSpecificationsTab,
                            scope: me.controller
                        }
                    },
                    {
                        title:  Uni.I18n.translate('deviceloadprofiles.readings', 'MDC', 'Readings'),
                        itemId: 'channel-data',
                        items: {
                            xtype: 'output-readings',
                            interval: me.interval,
                            output: me.output,
                            router: me.router
                        },
                        listeners: {
                            activate: me.controller.showReadingsTab,
                            scope: me.controller
                        },
                        usagePoint: me.usagePoint,
                        purpose: me.purpose,
                        output: me.output

                    }
                ],
                tabBar: {
                    layout: 'hbox',
                    items: [
                        {
                            xtype: 'tbfill'
                        },
                        {
                            xtype: 'previous-next-navigation-toolbar',
                            itemId: 'tabbed-device-channels-view-previous-next-navigation-toolbar',
                            store: 'Imt.purpose.store.Outputs',
                            router: me.router,
                            routerIdArgument: 'outputId',
                            itemsName: me.prevNextListLink,
                            indexLocation: 'arguments',
                            isFullTotalCount: true
                        }
                    ]
                }
            }
        ];

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                style: {
                    paddingRight: 0
                },
                items: [
                    {
                        xtype: 'usage-point-management-side-menu',
                        itemId: 'usage-point-management-side-menu',
                        router: me.router,
                        usagePoint: me.usagePoint,
                        purposes: me.purposes
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

    //showGraphView: function () {
    //    var me = this,
    //        dataStore = me.store,
    //        channelRecord = me.channel,
    //        container = me.down('deviceLoadProfileChannelGraphView'),
    //        zoomLevelsStore = Ext.getStore('Mdc.store.DataIntervalAndZoomLevels'),
    //        calculatedReadingType = channelRecord.get('calculatedReadingType'),
    //        channelName = calculatedReadingType && calculatedReadingType.fullAliasName ? calculatedReadingType.fullAliasName : '',
    //        unitOfMeasure = channelRecord.get('readingType').unit,
    //        seriesObject = {
    //            marker: {
    //                enabled: false
    //            },
    //            name: channelName
    //        },
    //        yAxis = {
    //            opposite: false,
    //            gridLineDashStyle: 'Dot',
    //            showEmpty: false,
    //            title: {
    //                rotation: 270,
    //                text: unitOfMeasure
    //            }
    //        },
    //        series = [],
    //        intervalRecord,
    //        zoomLevels,
    //        intervalLengthInMs;
    //
    //    seriesObject['data'] = [];
    //
    //    intervalRecord = zoomLevelsStore.getIntervalRecord(channelRecord.get('interval'));
    //    intervalLengthInMs = zoomLevelsStore.getIntervalInMs(channelRecord.get('interval'));
    //    zoomLevels = intervalRecord.get('zoomLevels');
    //
    //    switch (channelRecord.get('flowUnit')) {
    //        case 'flow':
    //            seriesObject['type'] = 'line';
    //            seriesObject['step'] = false;
    //            break;
    //        case 'volume':
    //            seriesObject['type'] = 'column';
    //            seriesObject['step'] = true;
    //            break;
    //    }
    //
    //    Ext.suspendLayouts();
    //    if (dataStore.getTotalCount() > 0) {
    //        var data = me.formatData();
    //        seriesObject['data'] = data.data;
    //        seriesObject['turboThreshold'] = Number.MAX_VALUE;
    //
    //        series.push(seriesObject);
    //        container.down('#graphContainer').show();
    //        container.drawGraph(yAxis, series, intervalLengthInMs, channelName, unitOfMeasure, zoomLevels, data.missedValues);
    //    } else {
    //        container.down('#graphContainer').hide();
    //    }
    //    me.updateLayout();
    //    Ext.resumeLayouts(true);
    //},
    //
    //formatData: function () {
    //    var me = this,
    //        data = [],
    //        missedValues = [],
    //        collectedUnitOfMeasure = me.channel.get('readingType').names.unitOfMeasure,
    //        calculatedUnitOfMeasure = me.channel.get('calculatedReadingType') ? me.channel.get('calculatedReadingType').names.unitOfMeasure : collectedUnitOfMeasure,
    //        okColor = "#70BB51",
    //        estimatedColor = "#568343",
    //        suspectColor = 'rgba(235, 86, 66, 1)',
    //        informativeColor = "#dedc49",
    //        notValidatedColor = "#71adc7",
    //        tooltipOkColor = 'rgba(255, 255, 255, 0.85)',
    //        tooltipSuspectColor = 'rgba(235, 86, 66, 0.3)',
    //        tooltipEstimatedColor = 'rgba(86, 131, 67, 0.3)',
    //        tooltipInformativeColor = 'rgba(222, 220, 73, 0.3)',
    //        tooltipNotValidatedColor = 'rgba(0, 131, 200, 0.3)';
    //
    //    me.store.each(function (record) {
    //        var point = {},
    //            interval = record.get('interval'),
    //            mainValidationInfo = record.get('mainValidationInfo'),
    //            bulkValidationInfo = record.get('bulkValidationInfo'),
    //            properties = record.get('readingProperties');
    //
    //        point.x = interval.start;
    //        point.id = point.x;
    //        point.y = parseFloat(record.get('value'));
    //        point.intervalEnd = interval.end;
    //        point.collectedValue = record.get('collectedValue');
    //        point.collectedUnitOfMeasure = collectedUnitOfMeasure;
    //        point.calculatedUnitOfMeasure = calculatedUnitOfMeasure;
    //        point.color = okColor;
    //        point.tooltipColor = tooltipOkColor;
    //        point.multiplier = record.get('multiplier');
    //
    //        if (mainValidationInfo.valueModificationFlag == 'EDITED') {
    //            point.edited = true;
    //        }
    //        if (mainValidationInfo.estimatedByRule) {
    //            point.color = estimatedColor;
    //            point.tooltipColor = tooltipEstimatedColor;
    //        } else if (properties.delta.notValidated) {
    //            point.color = notValidatedColor;
    //            point.tooltipColor = tooltipNotValidatedColor
    //        } else if (properties.delta.suspect) {
    //            point.color = suspectColor;
    //            point.tooltipColor = tooltipSuspectColor
    //        } else if (properties.delta.informative) {
    //            point.color = informativeColor;
    //            point.tooltipColor = tooltipInformativeColor;
    //        }
    //
    //        if (bulkValidationInfo.valueModificationFlag == 'EDITED') {
    //            point.bulkEdited = true;
    //        }
    //
    //        Ext.merge(point, properties);
    //        data.unshift(point);
    //
    //        !point.y && (point.y = null);
    //        if (!point.y) {
    //            if (properties.delta.suspect) {
    //                missedValues.push({
    //                    id: record.get('interval').start,
    //                    from: record.get('interval').start,
    //                    to: record.get('interval').end,
    //                    color: 'rgba(235, 86, 66, 0.3)'
    //                });
    //                record.set('plotBand', true);
    //            }
    //        }
    //    });
    //    return {data: data, missedValues: missedValues};
    //}
});
