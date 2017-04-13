/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicechannels.TabbedDeviceChannelsView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.tabbedDeviceChannelsView',
    itemId: 'tabbedDeviceChannelsView',
    requires: [
        'Uni.view.toolbar.PreviousNextNavigation',        
        'Mdc.view.setup.devicechannels.GraphView',
        'Mdc.view.setup.devicechannels.DataPreview',
        'Mdc.view.setup.devicechannels.DataGrid',
        'Uni.grid.FilterPanelTop',
        'Cfg.configuration.view.RuleWithAttributesForm'
    ],

    store: 'Mdc.store.ChannelOfLoadProfileOfDeviceData',

    mixins: {
        bindable: 'Ext.util.Bindable',
        graphWithGrid: 'Uni.util.GraphWithGrid',
        readingsGraph: 'Uni.util.ReadingsGraph'
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
    dataLoggerSlaveHistoryStore: null,
    idProperty: 'interval_end',
    validationConfigurationStore: undefined,
    estimationConfigurationStore: undefined,
    application: null,

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
                            device: me.device,
                            dataLoggerSlaveHistoryStore: me.dataLoggerSlaveHistoryStore
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
                                mentionDataLoggerSlave: me.mentionDataLoggerSlave,
                                listeners: {
                                    barselect: Ext.bind(me.onBarSelect, me)
                                }
                            },
                            {
                                xtype: 'preview-container',
                                itemId: 'channel-data-preview-container',
                                grid: {
                                    xtype: 'deviceLoadProfileChannelDataGrid',
                                    channelRecord: me.channel,
                                    router: me.router,
                                    listeners: {
                                        select: function (grid, record) {
                                            me.down('#channel-data-preview-container').fireEvent('rowselect', record);
                                        },
                                        itemclick: function (dataView, record) {
                                            if (me.down('deviceLoadProfileChannelDataGrid').getSelectionModel().isSelected(record)) {
                                                me.down('#channel-data-preview-container').fireEvent('rowselect', record);
                                            }
                                        }
                                    }
                                },
                                previewComponent: {
                                    xtype: 'deviceLoadProfileChannelDataPreview',
                                    channelRecord: me.channel,
                                    router: me.router,
                                    mentionDataLoggerSlave: !Ext.isEmpty(me.device) && !Ext.isEmpty(me.device.get('isDataLogger')) && me.device.get('isDataLogger'),
                                    hidden: true
                                },
                                emptyComponent: {
                                    xtype: 'uni-form-empty-message',
                                    itemId: 'ctr-table-no-data',
                                    text: Uni.I18n.translate('deviceloadprofiles.data.empty', 'MDC', 'No readings have been defined yet.')
                                },
                                listeners: {
                                    rowselect: Ext.bind(me.onRowSelect, me)
                                }
                            }
                        ]
                    }
                ],
                listeners: {
                    afterrender: function (panel) {
                        var bar = panel.tabBar;
                        bar.add([
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

        if (Mdc.privileges.Device.canViewValidationConfiguration()) {
            me.addValidationConfiguration();
        }
        if (Mdc.privileges.Device.canViewEstimationConfiguration()) {
            me.addEstimationConfiguration();
        }

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

    addValidationConfiguration: function () {
        var me = this,
            validationConfigurationRecord = me.validationConfigurationStore.first(),
            valRulesForCollectedReadingTypeStore = validationConfigurationRecord.rulesForCollectedReadingType(),
            valRulesForCalculatedReadingTypeStore = validationConfigurationRecord.rulesForCalculatedReadingType(),
            validationRuleWithAttributesForms = [];

        if (valRulesForCollectedReadingTypeStore.getCount()) {
            validationRuleWithAttributesForms.push(me.prepareForm('validation', valRulesForCollectedReadingTypeStore, 'collected'));
        }

        if (valRulesForCalculatedReadingTypeStore.getCount()) {
            validationRuleWithAttributesForms.push(me.prepareForm('validation', valRulesForCalculatedReadingTypeStore, 'calculated'));
        }

        if (validationRuleWithAttributesForms.length) {
            me.content[0].items.push({
                title: Uni.I18n.translate('general.validationConfiguration', 'MDC', 'Validation configuration'),
                itemId: 'channel-validation-configuration',
                items: validationRuleWithAttributesForms
            });
        }
    },

    addEstimationConfiguration: function () {
        var me = this,
            estimationConfigurationRecord = me.estimationConfigurationStore.first(),
            estRulesForCollectedReadingTypeStore = estimationConfigurationRecord.rulesForCollectedReadingType(),
            estRulesForCalculatedReadingTypeStore = estimationConfigurationRecord.rulesForCalculatedReadingType(),
            estimationRuleWithAttributesForms = [];

        if (estRulesForCollectedReadingTypeStore.getCount()) {
            estimationRuleWithAttributesForms.push(me.prepareForm('estimation', estRulesForCollectedReadingTypeStore, 'collected'));
        }

        if (estRulesForCalculatedReadingTypeStore.getCount()) {
            estimationRuleWithAttributesForms.push(me.prepareForm('estimation', estRulesForCalculatedReadingTypeStore, 'calculated'));
        }

        if (estimationRuleWithAttributesForms.length) {
            me.content[0].items.push({
                title: Uni.I18n.translate('general.estimationConfiguration', 'MDC', 'Estimation configuration'),
                itemId: 'channel-estimation-configuration',
                items: estimationRuleWithAttributesForms
            });
        }
    },

    prepareForm: function (type, store, kindOfReadingType) {
        var me = this,
            titleToken = type === 'validation' ? Uni.I18n.translate('general.validationConfigurationFor', 'MDC', 'Validation configuration for')
                : Uni.I18n.translate('general.estimationConfigurationFor', 'MDC', 'Estimation configuration for'),
            hasAdministerPrivileges = type === 'validation' ? Mdc.privileges.Device.canAdministerValidationConfiguration() : Mdc.privileges.Device.canAdministerEstimationConfiguration();

        return {
            xtype: 'rule-with-attributes-form',
            itemId: 'rule-with-attributes-channel-' + type + '-form-' + kindOfReadingType,
            kindOfReadingType: kindOfReadingType,
            router: me.router,
            records: store.getRange(),
            type: type,
            ui: 'medium',
            title: Ext.String.format("{0} {1}", titleToken, store.first().get('readingType').fullAliasName),
            application: me.application,
            hasAdministerPrivileges: hasAdministerPrivileges
        };
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
            point.showDeviceQualityIcon = !Ext.isEmpty(record.get('readingQualities'));

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
                point.dataLoggerSlave = Ext.String.htmlEncode(slaveChannelInfo.deviceName);
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
    },

    getValueFromPoint: function (point) {
        return new Date(point.intervalEnd);
    }
});
