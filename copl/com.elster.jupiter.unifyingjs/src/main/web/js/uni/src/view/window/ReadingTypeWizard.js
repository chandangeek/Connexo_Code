/**
 * @class Uni.view.window.ReadingTypeWizard
 */
Ext.define('Uni.view.window.ReadingTypeWizard', {
    extend: 'Uni.view.window.Wizard',

    requires: [
        'Ext.form.RadioGroup'
    ],

    width: 800,
    height: 600,

    title: Uni.I18n.translate('window.readingtypewizard.title', 'UNI', 'Select a reading type'),

    description: {
        xtype: 'container',
        layout: 'vbox',
        items: [
            {
                xtype: 'component',
                html: Uni.I18n.translate('window.readingtypewizard.description', 'UNI',
                    'Use the steps below to define a value for the different attributes of a reading type'
                )
            },
            {
                xtype: 'component',
                html: 'TODO'
            }
        ]
    },

    initComponent: function () {
        // Sadly, the Ext.clone function is not enough to create a duplicate of a store.
        var intervalMinuteStore = Ext.create('Ext.data.Store', {
                fields: ['text', 'value']
            }),
            intervalHourStore = Ext.create('Ext.data.Store', {
                fields: ['text', 'value']
            }),
            fixedBlockStore = Ext.create('Ext.data.Store', {
                fields: ['text', 'value']
            }),
            rollingBlockStore = Ext.create('Ext.data.Store', {
                fields: ['text', 'value']
            });

        this.initMeasuringPeriodForm(
            intervalMinuteStore,
            intervalHourStore,
            fixedBlockStore,
            rollingBlockStore
        );

        var commodityElecStore = Ext.create('Ext.data.Store', {
                fields: ['text', 'value']
            }),
            commodityFluidStore = Ext.create('Ext.data.Store', {
                fields: ['text', 'value']
            }),
            commodityGasStore = Ext.create('Ext.data.Store', {
                fields: ['text', 'value']
            }),
            commodityMatterStore = Ext.create('Ext.data.Store', {
                fields: ['text', 'value']
            }),
            commodityOtherStore = Ext.create('Ext.data.Store', {
                fields: ['text', 'value']
            });

        this.initCommodityForm(
            commodityElecStore,
            commodityFluidStore,
            commodityGasStore,
            commodityMatterStore,
            commodityOtherStore
        );

        this.steps = [
            {
                title: Uni.I18n.translate('window.readingtypewizard.introduction', 'UNI', 'Introduction'),
                xtype: 'container',
                layout: {
                    type: 'vbox',
                    align: 'stretchmax'
                },
                items: [
                    {
                        xtype: 'component',
                        html: Uni.I18n.translate('window.readingtypewizard.introduction.content', 'UNI',
                            '<p>A reading type provides a detailed description of a reading value. It is described in ' +
                                'terms of 18 key attributes.</p>' +
                                '<p>Every attribute that has a value of zero is not applicable to the description.</p>' +
                                '<p>Step through this wizard to define a value for each attribute or compound attribute ' +
                                'of the reading type. You can skip steps or jump to a specific step by using the ' +
                                'navigation on the left.</p>'
                        )
                    }
                ]
            },
            {
                title: Uni.I18n.translate('window.readingtypewizard.macroperiod', 'UNI', 'Macro period'),
                xtype: 'container',
                layout: {
                    type: 'anchor'
                },
                items: [
                    {
                        xtype: 'component',
                        html: '<p>' + Uni.I18n.translate('window.readingtypewizard.macroperiod.description', 'UNI',
                            'Reflects how the data is viewed or captured over a long period of time.') + '</p>'
                    },
                    {
                        xtype: 'radiogroup',
                        itemId: 'macroPeriod',
                        columns: 1,
                        anchor: '100%',
                        items: [
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.notapplicable',
                                'UNI', 'Not applicable (0)'), name: 'macroPeriod', inputValue: 0, checked: true},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.macroperiod.daily',
                                'UNI', 'Daily (11)'), name: 'macroPeriod', inputValue: 11},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.macroperiod.weekly',
                                'UNI', 'Weekly (24)'), name: 'macroPeriod', inputValue: 24},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.macroperiod.monthly',
                                'UNI', 'Monthly (13)'), name: 'macroPeriod', inputValue: 13},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.macroperiod.seasonal',
                                'UNI', 'Seasonal (22)'), name: 'macroPeriod', inputValue: 22},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.macroperiod.billingperiod',
                                'UNI', 'Billing period (8)'), name: 'macroPeriod', inputValue: 8},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.macroperiod.specifiedperiod',
                                'UNI', 'Specified period (32)'), name: 'macroPeriod', inputValue: 32}
                        ]
                    }
                ]
            },
            {
                title: Uni.I18n.translate('window.readingtypewizard.dataaggregation', 'UNI', 'Data aggregation'),
                xtype: 'container',
                layout: {
                    type: 'anchor'
                },
                items: [
                    {
                        xtype: 'component',
                        html: '<p>' + Uni.I18n.translate('window.readingtypewizard.dataaggregation.description',
                            'UNI', 'DMay be used to define a mathematical operation carried out over the ' +
                                'time period (#1).') + '</p>'
                    },
                    {
                        xtype: 'radiogroup',
                        itemId: 'dataAggregation',
                        columns: 1,
                        anchor: '100%',
                        items: [
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.notapplicable',
                                'UNI', 'Not applicable (0)'), name: 'dataAggregation', inputValue: 0, checked: true},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaggregation.normal',
                                'UNI', 'Normal (12)'), name: 'dataAggregation', inputValue: 12},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaggregation.nominal',
                                'UNI', 'Nominal (11)'), name: 'dataAggregation', inputValue: 11},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaggregation.average',
                                'UNI', 'Average (2)'), name: 'dataAggregation', inputValue: 2},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaggregation.sum',
                                'UNI', 'Sum (26)'), name: 'dataAggregation', inputValue: 26},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaggregation.excess',
                                'UNI', 'Excess (4)'), name: 'dataAggregation', inputValue: 4},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaggregation.lowthreshold',
                                'UNI', 'Low threshold (7)'), name: 'dataAggregation', inputValue: 7},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaggregation.highthreshold',
                                'UNI', 'High threshold (5)'), name: 'dataAggregation', inputValue: 5},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaggregation.low',
                                'UNI', 'Low (28)'), name: 'dataAggregation', inputValue: 28},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaggregation.minimum',
                                'UNI', 'Minimum (28)'), name: 'dataAggregation', inputValue: 9},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaggregation.secondminimum',
                                'UNI', 'Second minimum (17)'), name: 'dataAggregation', inputValue: 17},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaggregation.maximum',
                                'UNI', 'Maximum (16)'), name: 'dataAggregation', inputValue: 8},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaggregation.secondmaximum',
                                'UNI', 'Second maximum (16)'), name: 'dataAggregation', inputValue: 16},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaggregation.thirdmaximum',
                                'UNI', 'Third maximum (23)'), name: 'dataAggregation', inputValue: 23},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaggregation.fourthmaximum',
                                'UNI', 'Fourth maximum (24)'), name: 'dataAggregation', inputValue: 24},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaggregation.fifthmaximum',
                                'UNI', 'Fifth maximum (25)'), name: 'dataAggregation', inputValue: 25},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaggregation.high',
                                'UNI', 'High (27)'), name: 'dataAggregation', inputValue: 27}
                        ]
                    }
                ]
            },
            {
                title: Uni.I18n.translate('window.readingtypewizard.measuringperiod', 'UNI', 'Measuring period'),
                xtype: 'container',
                layout: {
                    type: 'anchor'
                },
                items: [
                    {
                        xtype: 'component',
                        html: '<p>' + Uni.I18n.translate('window.readingtypewizard.measuringperiod.description', 'UNI',
                            'Describes the way the value was originally measured. This doesn\'t represent the ' +
                                'frequency at which it is reported or presented.') + '</p>'
                    },
                    {
                        xtype: 'radiogroup',
                        itemId: 'measuringPeriod',
                        columns: 1,
                        anchor: '100%',
                        items: [
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.notapplicable',
                                'UNI', 'Not applicable (0)'), name: 'measuringPeriod', inputValue: 0, checked: true},

                            {
                                xtype: 'component',
                                html: '<p>' + Uni.I18n.translate('window.readingtypewizard.measuringperiod.interval',
                                    'UNI', 'Interval') + '</p>'
                            },
                            {
                                xtype: 'container',
                                layout: 'hbox',
                                items: [
                                    {
                                        xtype: 'radiofield',
                                        name: 'measuringPeriod',
                                        inputValue: 'intervalMinute'
                                    },
                                    {
                                        xtype: 'combobox',
                                        itemId: 'intervalMinute',
                                        displayField: 'text',
                                        valueField: 'value',
                                        queryMode: 'local',
                                        store: intervalMinuteStore
                                    }
                                ]
                            },
                            {
                                xtype: 'container',
                                layout: 'hbox',
                                items: [
                                    {
                                        xtype: 'radiofield',
                                        name: 'measuringPeriod',
                                        inputValue: 'intervalHour'
                                    },
                                    {
                                        xtype: 'combobox',
                                        itemId: 'intervalHour',
                                        displayField: 'text',
                                        valueField: 'value',
                                        queryMode: 'local',
                                        store: intervalHourStore
                                    }
                                ]
                            },
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaggregation.specifiedinterval',
                                'UNI', 'Specified interval (100)'), name: 'measuringPeriod', inputValue: 100},

                            {
                                xtype: 'component',
                                html: '<p>' + Uni.I18n.translate('window.readingtypewizard.measuringperiod.fixedblock',
                                    'UNI', 'Fixed block') + '</p>'
                            },
                            {
                                xtype: 'container',
                                layout: 'hbox',
                                items: [
                                    {
                                        xtype: 'radiofield',
                                        name: 'measuringPeriod',
                                        inputValue: 'fixedBlock'
                                    },
                                    {
                                        xtype: 'combobox',
                                        itemId: 'fixedBlock',
                                        displayField: 'text',
                                        valueField: 'value',
                                        queryMode: 'local',
                                        store: fixedBlockStore
                                    }
                                ]
                            },
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.measuringperiod.specifiedfixedblock',
                                'UNI', 'Specified fixed block (101)'), name: 'measuringPeriod', inputValue: 101},

                            {
                                xtype: 'component',
                                html: '<p>' + Uni.I18n.translate('window.readingtypewizard.measuringperiod.fixedblock',
                                    'UNI', 'Fixed block') + '</p>'
                            },
                            {
                                xtype: 'container',
                                layout: 'hbox',
                                items: [
                                    {
                                        xtype: 'radiofield',
                                        name: 'measuringPeriod',
                                        inputValue: 'rollingBlock'
                                    },
                                    {
                                        xtype: 'combobox',
                                        itemId: 'rollingBlock',
                                        displayField: 'text',
                                        valueField: 'value',
                                        queryMode: 'local',
                                        store: rollingBlockStore
                                    }
                                ]
                            },
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.measuringperiod.specifiedrollingblock',
                                'UNI', 'Specified rolling block (102)'), name: 'measuringPeriod', inputValue: 102}
                        ]
                    }
                ]
            },
            {
                title: Uni.I18n.translate('window.readingtypewizard.dataaccumulation', 'UNI', 'Data accumulation'),
                xtype: 'container',
                layout: {
                    type: 'anchor'
                },
                items: [
                    {
                        xtype: 'component',
                        html: '<p>' + Uni.I18n.translate('window.readingtypewizard.dataaccumulation.description', 'UNI',
                            'Indicates how the value is represented to accumulate over time.') + '</p>'
                    },
                    {
                        xtype: 'radiogroup',
                        itemId: 'dataAccumulation',
                        columns: 1,
                        anchor: '100%',
                        items: [
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.notapplicable',
                                'UNI', 'Not applicable (0)'), name: 'dataAccumulation', inputValue: 0, checked: true},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaccumulation.bulkquantity',
                                'UNI', 'Bulk quantity (1)'), name: 'dataAccumulation', inputValue: 1},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaccumulation.deltadata',
                                'UNI', 'Delta data (4)'), name: 'dataAccumulation', inputValue: 4},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaccumulation.cumulative',
                                'UNI', 'Cumulative (3)'), name: 'dataAccumulation', inputValue: 3},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaccumulation.continiouscumulative',
                                'UNI', 'Continious cumulative (2)'), name: 'dataAccumulation', inputValue: 2},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaccumulation.indicating',
                                'UNI', 'Indicating (6)'), name: 'dataAccumulation', inputValue: 6},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaccumulation.summation',
                                'UNI', 'Summation (9)'), name: 'dataAccumulation', inputValue: 9},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaccumulation.timedelay',
                                'UNI', 'Time delay (10)'), name: 'dataAccumulation', inputValue: 10},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaccumulation.instantaneous',
                                'UNI', 'Instantaneous (12)'), name: 'dataAccumulation', inputValue: 12},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaccumulation.latchingquantity',
                                'UNI', 'Latching quantity (13)'), name: 'dataAccumulation', inputValue: 13},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaccumulation.boundedquantity',
                                'UNI', 'Bounded quantity (14)'), name: 'dataAccumulation', inputValue: 14}
                        ]
                    }
                ]
            },
            {
                title: Uni.I18n.translate('window.readingtypewizard.flowdirection', 'UNI', 'Flow direction'),
                xtype: 'container',
                layout: {
                    type: 'anchor'
                },
                items: [
                    {
                        xtype: 'component',
                        html: '<p>' + Uni.I18n.translate('window.readingtypewizard.flowdirection.description', 'UNI',
                            'Indicates how the value is represented to accumulate over time.') + '</p>'
                    },
                    {
                        xtype: 'radiogroup',
                        itemId: 'flowDirection',
                        columns: 1,
                        anchor: '100%',
                        items: [
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.notapplicable',
                                'UNI', 'Not applicable (0)'), name: 'flowDirection', inputValue: 0, checked: true},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.flowdirection.forward',
                                'UNI', 'Forward (1)'), name: 'flowDirection', inputValue: 1},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.flowdirection.reverse',
                                'UNI', 'Reverse (19)'), name: 'flowDirection', inputValue: 19},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.flowdirection.lagging',
                                'UNI', 'Lagging (2)'), name: 'flowDirection', inputValue: 2},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.flowdirection.leading',
                                'UNI', 'Leading (3)'), name: 'flowDirection', inputValue: 3},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.flowdirection.net',
                                'UNI', 'Net (4)'), name: 'flowDirection', inputValue: 4},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.flowdirection.total',
                                'UNI', 'Total (20)'), name: 'flowDirection', inputValue: 20},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.flowdirection.totalbyphase',
                                'UNI', 'Total by phase (21)'), name: 'flowDirection', inputValue: 21},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.flowdirection.quadrant1',
                                'UNI', 'Quadrant 1 (15)'), name: 'flowDirection', inputValue: 15},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.flowdirection.quadrants1and2',
                                'UNI', 'Quadrants 1 and 2 (5)'), name: 'flowDirection', inputValue: 5},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.flowdirection.quadrants1and3',
                                'UNI', 'Quadrants 1 and 3 (7)'), name: 'flowDirection', inputValue: 7},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.flowdirection.quadrants1and4',
                                'UNI', 'Quadrants 1 and 4 (8)'), name: 'flowDirection', inputValue: 8},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.flowdirection.quadrants1minus4',
                                'UNI', 'Quadrants 1 minus 4 (9)'), name: 'flowDirection', inputValue: 9},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.flowdirection.quadrant2',
                                'UNI', 'Quadrant 2 (16)'), name: 'flowDirection', inputValue: 16},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.flowdirection.quadrants2and3',
                                'UNI', 'Quadrant 2 and 3 (10)'), name: 'flowDirection', inputValue: 10},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.flowdirection.quadrants2and4',
                                'UNI', 'Quadrant 2 and 4 (11)'), name: 'flowDirection', inputValue: 11},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.flowdirection.quadrants2minus3',
                                'UNI', 'Quadrant 2 minus 3 (12)'), name: 'flowDirection', inputValue: 12},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.flowdirection.quadrant3',
                                'UNI', 'Quadrant 3 (17)'), name: 'flowDirection', inputValue: 17},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.flowdirection.quadrants3and4',
                                'UNI', 'Quadrants 3 and 4 (13)'), name: 'flowDirection', inputValue: 13},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.flowdirection.quadrants3minus2',
                                'UNI', 'Quadrants 3 minus 2 (14)'), name: 'flowDirection', inputValue: 14},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.flowdirection.quadrant4',
                                'UNI', 'Bounded quantity (18)'), name: 'flowDirection', inputValue: 18}
                        ]
                    }
                ]
            },
            {
                title: Uni.I18n.translate('readingType.commodity', 'UNI', 'Commodity'),
                xtype: 'container',
                layout: {
                    type: 'anchor'
                },
                items: [
                    {
                        xtype: 'component',
                        html: '<p>' + Uni.I18n.translate('window.readingtypewizard.commodity.description',
                            'UNI', 'Some description.') + '</p>'
                    },
                    {
                        xtype: 'radiogroup',
                        itemId: 'commodity',
                        columns: 1,
                        anchor: '100%',
                        items: [
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.notapplicable',
                                'UNI', 'Not applicable (0)'), name: 'commodity', inputValue: 0, checked: true}
                        ]
                    }
                ]
            },
            {
                title: Uni.I18n.translate('window.readingtypewizard.measurementkind', 'UNI', 'Measurement kind'),
                xtype: 'container',
                layout: 'vbox',
                items: [
                    {
                        xtype: 'component',
                        html: '<h3>Measurement kind</h3>'
                    }
                ]
            },
            {
                title: Uni.I18n.translate('window.readingtypewizard.interharmonics', 'UNI', 'Interharmonics'),
                xtype: 'container',
                layout: 'vbox',
                items: [
                    {
                        xtype: 'component',
                        html: '<h3>Interharmonics</h3>'
                    }
                ]
            },
            {
                title: Uni.I18n.translate('window.readingtypewizard.argument', 'UNI', 'Argument'),
                xtype: 'container',
                layout: 'vbox',
                items: [
                    {
                        xtype: 'component',
                        html: '<h3>Argument</h3>'
                    }
                ]
            },
            {
                title: Uni.I18n.translate('readingType.timeOfUse', 'UNI', 'Time of use'),
                xtype: 'container',
                layout: 'vbox',
                items: [
                    {
                        xtype: 'component',
                        html: '<h3>Time of use</h3>'
                    }
                ]
            },
            {
                title: Uni.I18n.translate('readingType.criticalPeakPeriod', 'UNI', 'Critical peak period'),
                xtype: 'container',
                layout: 'vbox',
                items: [
                    {
                        xtype: 'component',
                        html: '<h3>Critical peak period</h3>'
                    }
                ]
            },
            {
                title: Uni.I18n.translate('readingType.comsumptionTier', 'UNI', 'Consumption tier'),
                xtype: 'container',
                layout: 'vbox',
                items: [
                    {
                        xtype: 'component',
                        html: '<h3>Consumption tier</h3>'
                    }
                ]
            },
            {
                title: Uni.I18n.translate('readingType.phase', 'UNI', 'Phase'),
                xtype: 'container',
                layout: 'vbox',
                items: [
                    {
                        xtype: 'component',
                        html: '<h3>Phase</h3>'
                    }
                ]
            },
            {
                title: Uni.I18n.translate('readingType.multiplier', 'UNI', 'Multiplier'),
                xtype: 'container',
                layout: 'vbox',
                items: [
                    {
                        xtype: 'component',
                        html: '<h3>Multiplier</h3>'
                    }
                ]
            },
            {
                title: Uni.I18n.translate('readingType.unitOfMeasure', 'UNI', 'Unit of measure'),
                xtype: 'container',
                layout: 'vbox',
                items: [
                    {
                        xtype: 'component',
                        html: '<h3>Unit of measure</h3>'
                    }
                ]
            },
            {
                title: Uni.I18n.translate('readingType.currency', 'UNI', 'Currency'),
                xtype: 'container',
                layout: 'vbox',
                items: [
                    {
                        xtype: 'component',
                        html: '<h3>Currency</h3>'
                    }
                ]
            }
        ];

        this.callParent(arguments);
    },

    initMeasuringPeriodForm: function (intervalMinuteStore, intervalHourStore, fixedBlockStore, rollingBlockStore) {
        this.populateSimpleTypeStore(intervalMinuteStore, 'window.readingtypewizard.minute', '{0} minutes', [
            [1, 3], // value, enumeration
            [2, 10],
            [3, 14],
            [5, 6],
            [10, 1],
            [12, 78],
            [15, 2],
            [20, 31],
            [30, 5],
            [60, 7]
        ]);

        this.populateSimpleTypeStore(intervalHourStore, 'window.readingtypewizard.hour', '{0} hours', [
            [2, 79], // value, enumeration
            [3, 83],
            [4, 80],
            [6, 81],
            [12, 82],
            [24, 4]
        ]);

        this.populateSimpleTypeStore(fixedBlockStore, 'window.readingtypewizard.minutefixed', '{0} minutes fixed block', [
            [1, 56], // value, enumeration
            [5, 55],
            [10, 54],
            [15, 53],
            [20, 52],
            [30, 51],
            [60, 50]
        ]);

        this.populateRollingBlockStore(rollingBlockStore, 'window.readingtypewizard.minuterolling', '{0} minutes rolling block with {1} minute subintervals', [
            [
                [60, 30], // values
                57 // enumeration
            ],
            [
                [60, 20],
                58
            ],
            [
                [60, 15],
                59
            ],
            [
                [60, 12],
                60
            ],
            [
                [60, 10],
                61
            ],
            [
                [60, 6],
                62
            ],
            [
                [60, 5],
                63
            ],
            [
                [60, 4],
                64
            ],
            [
                [30, 15],
                65
            ],
            [
                [30, 10],
                66
            ],
            [
                [30, 6],
                67
            ],
            [
                [30, 5],
                68
            ],
            [
                [30, 3],
                69
            ],
            [
                [30, 2],
                70
            ],
            [
                [15, 5],
                71
            ],
            [
                [15, 3],
                72
            ],
            [
                [15, 1],
                73
            ]
        ]);
    },

    initCommodityForm: function (commodityElecStore, commodityFluidStore, commodityGasStore, commodityMatterStore, commodityOtherStore) {
        var baseKey = 'window.readingtypewizard.commodity.';

        this.populateKeyValueStore(commodityElecStore, [
            [baseKey + 'electricityprimarymetered', 'fallback', 1]
        ]);

        this.populateKeyValueStore(commodityFluidStore, [
            ['key', 'fallback', 1]
        ]);

        this.populateKeyValueStore(commodityGasStore, [
            ['key', 'fallback', 1]
        ]);

        this.populateKeyValueStore(commodityMatterStore, [
            ['key', 'fallback', 1]
        ]);

        this.populateKeyValueStore(commodityOtherStore, [
            ['key', 'fallback', 1]
        ]);
    },

    populateSimpleTypeStore: function (store, key, fallback, data) {
        for (var i = 0; i < data.length; i++) {
            var obj = data[i],
                value = obj[0],
                enumeration = obj[1];

            var addition = {
                text: Uni.I18n.translatePlural(key, value, 'UNI', fallback) + ' (' + enumeration + ')',
                value: enumeration
            };

            store.add(addition);
        }
    },

    populateRollingBlockStore: function (store, key, fallback, data) {
        for (var i = 0; i < data.length; i++) {
            var obj = data[i],
                values = obj[0],
                enumeration = obj[1];

            var addition = {
                text: Uni.I18n.translate(key, 'UNI', fallback, values) + ' (' + enumeration + ')',
                value: enumeration
            };

            store.add(addition);
        }
    },

    populateKeyValueStore: function (store, data) {
        for (var i = 0; i < data.length; i++) {
            var obj = data[i],
                key = obj[0],
                fallback = obj[1],
                enumeration = obj[2];

            var addition = {
                text: Uni.I18n.translate(key, 'UNI', fallback) + ' (' + enumeration + ')',
                value: enumeration
            };

            store.add(addition);
        }
    }

});