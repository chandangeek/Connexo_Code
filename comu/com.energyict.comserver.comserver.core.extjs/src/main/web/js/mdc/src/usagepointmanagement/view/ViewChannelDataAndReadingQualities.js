Ext.define('Mdc.usagepointmanagement.view.ViewChannelDataAndReadingQualities', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.view-channel-data-and-reading-qualities',
    requires: [
        'Uni.grid.FilterPanelTop',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Uni.view.container.PreviewContainer',
        'Mdc.usagepointmanagement.view.UsagePointSideMenu',
        'Mdc.usagepointmanagement.view.ChannelDataGraph',
        'Mdc.usagepointmanagement.view.ChannelDataGrid',
        'Mdc.usagepointmanagement.view.ChannelDataPreview'
    ],
    mixins: {
        bindable: 'Ext.util.Bindable',
        graphWithGrid: 'Uni.util.GraphWithGrid',
        readingsGraph: 'Uni.util.ReadingsGraph'
    },
    store: 'Mdc.usagepointmanagement.store.ChannelData',

    router: null,
    channel: null,
    usagePointId: null,
    filter: null,
    idProperty: 'interval_end',

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                itemId: 'view-channel-data-and-reading-qualities-panel',
                title: me.channel.get('readingType').fullAliasName,
                items: [
                    {
                        xtype: 'previous-next-navigation-toolbar',
                        itemId: 'tabbed-device-channels-view-previous-next-navigation-toolbar',
                        store: 'Mdc.usagepointmanagement.store.Channels',
                        router: me.router,
                        routerIdArgument: 'channelId',
                        itemsName: Ext.String.format('<a href="{0}">{1}</a>',
                            me.router.getRoute('usagepoints/usagepoint/channels').buildUrl(),
                            Uni.I18n.translate('general.channels', 'MDC', 'Channels').toLowerCase()),
                        isFullTotalCount: true
                    },
                    {
                        xtype: 'uni-grid-filterpaneltop',
                        itemId: 'channel-data-top-filter',
                        store: 'Mdc.usagepointmanagement.store.ChannelData',
                        hasDefaultFilters: true,
                        filters: [
                            {
                                type: 'duration',
                                text: Uni.I18n.translate('general.startDate', 'MDC', 'Start date'),
                                itemId: 'channel-data-top-filter-duration',
                                dataIndex: 'interval',
                                dataIndexFrom: 'intervalStart',
                                dataIndexTo: 'intervalEnd',
                                defaultFromDate: me.filter.interval.getIntervalStart((me.channel.get('lastReading') || new Date())),
                                defaultDuration: me.filter.defaultDuration,
                                durationStore: me.filter.durationStore,
                                loadStore: false
                            }
                        ]
                    },
                    {
                        xtype: 'channel-data-graph',
                        itemId: 'channel-data-graph',
                        store: Ext.getStore(me.store),
                        channel: me.channel,
                        zoomLevels: me.filter.interval.get('zoomLevels'),
                        listeners: {
                            barselect: Ext.bind(me.onBarSelect, me)
                        }
                    },
                    {
                        xtype: 'preview-container',
                        itemId: 'readings-preview-container',
                        grid: {
                            xtype: 'channel-data-grid',
                            itemId: 'channel-data-grid',
                            store: me.store,
                            channel: me.channel,
                            viewConfig: {
                                loadMask: false,
                                doFocus: Ext.emptyFn // workaround to avoid page jump during row selection
                            },
                            listeners: {
                                select: function (grid, record) {
                                    me.down('#readings-preview-container').fireEvent('rowselect', record);
                                },
                                itemclick: function (dataView, record) {
                                    if (me.down('channel-data-grid').getSelectionModel().isSelected(record)) {
                                        me.down('#readings-preview-container').fireEvent('rowselect', record);
                                    }
                                }                                
                            }
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            title: Uni.I18n.translate('usagePointChannelData.empty.title', 'MDC', 'No data is available'),
                            reasons: [
                                Uni.I18n.translate('usagePointChannelData.empty.list.item1', 'MDC', 'No data has been collected yet'),
                                Uni.I18n.translate('usagePointChannelData.empty.list.item2', 'MDC', 'No devices have been linked to this usage point in specified period of time')
                            ]
                        },
                        previewComponent: {
                            xtype: 'channel-data-preview',
                            itemId: 'channel-data-preview',
                            router: me.router,
                            channel: me.channel
                        },
                        listeners: {
                            rowselect: Ext.bind(me.onRowSelect, me)
                        }
                    }
                ]
            }
        ];

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'usage-point-management-side-menu',
                        itemId: 'usage-point-management-side-menu',
                        router: me.router,
                        usagePointId: me.usagePointId
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

    formatData: function () {
        var me = this,
            data = [],
            missedValues = [],
            unit = me.channel.get('readingType').names.unitOfMeasure,
            validationMap = {
                NOT_VALIDATED: {
                    barColor: 'rgba(113,173,199,1)',
                    tooltipColor: 'rgba(0,131,200,0.3)',
                    icon: '<span class="icon-flag6"></span>'
                },
                SUSPECT: {
                    barColor: 'rgba(235,86,66,1)',
                    tooltipColor: 'rgba(235,86,66,0.3)',
                    icon: '<span class="icon-flag5" style="color:red"></span>'
                },
                INFORMATIVE: {
                    barColor: 'rgba(222,220,73,1)',
                    tooltipColor: 'rgba(222,220,73,0.3)',
                    icon: '<span class="icon-flag5" style="color:yellow"></span>'
                },
                OK: {
                    barColor: 'rgba(112,187,81,1)',
                    tooltipColor: 'rgba(255,255,255,0.85)',
                    icon: ''
                },
                NO_LINKED_DEVICES: {
                    barColor: null,
                    tooltipColor: null,
                    icon: ''
                }
            };

        me.store.each(function (record) {
            var point = {},
                interval = record.get('interval'),
                validation = record.get('validation');

            point.x = interval.start;
            point.id = point.x;
            point.y = parseFloat(record.get('value')) || null;
            point.intervalEnd = interval.end;
            point.color = validationMap[validation].barColor;
            point.tooltipColor = validationMap[validation].tooltipColor;
            point.icon = validationMap[validation].icon;
            point.unit = unit;
            //point.multiplier = record.get('multiplier');

            data.unshift(point);
            !point.y && (point.y = null);
            if (!point.y) {
                if (validation === 'SUSPECT' || validation === 'NO_LINKED_DEVICES') {
                    missedValues.push({
                        id: interval.start,
                        from: interval.start,
                        to: interval.end,
                        color: validation === 'SUSPECT' ? 'rgba(235, 86, 66, 0.3)' : 'rgba(210,210,210,1)'
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