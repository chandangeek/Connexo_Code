/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
/**
 * Factory for combo processor objects.
 */
Ext.define('Mtr.controller.readingtypesgroup.processors.ComboProcessorFactory', {

    config: {
        controller: null
    },

    // Cache combo processor instances
    processorsMap: new Ext.util.HashMap(),

    constructor: function (config) {
        this.initConfig(config);
    },

    getProcessor: function (combo) {
        var me = this,
            type = combo.name,
            processor = me.processorsMap.get(type);

        return processor ? processor : me.createProcessor(combo);
    },


    createProcessor: function (combo) {
        var me = this,
            type = combo.name,
            processor = null,
            cloneValue = me.controller.cimHandler.getValue(combo.cimIndex);

        switch (type) {
            case "basicCommodity":
                processor = Ext.create('Mtr.controller.readingtypesgroup.processors.CommodityProcessor', {
                    cloneValue: cloneValue,
                    controller: me.controller
                });
                break;
            case "basicMeasurementKind":
                processor = Ext.create('Mtr.controller.readingtypesgroup.processors.MeasurementKindProcessor', {
                    cloneValue: cloneValue,
                    controller: me.controller
                });
                break;
            case "basicFlowDirection":
                processor = Ext.create('Mtr.controller.readingtypesgroup.processors.FlowProcessor', {
                    cloneValue: cloneValue,
                    controller: me.controller
                });
                break;
            case "basicUnit":
                processor = Ext.create('Mtr.controller.readingtypesgroup.processors.UnitProcessor', {
                    cloneValue: cloneValue,
                    controller: me.controller
                });
                break;
            case "basicMacroPeriod":
                processor = Ext.create('Mtr.controller.readingtypesgroup.processors.MacroPeriodProcessor', {
                    cloneValue: cloneValue,
                    controller: me.controller
                });
                break;
            case "basicAggregate":
                processor = Ext.create('Mtr.controller.readingtypesgroup.processors.AggregateProcessor', {
                    cloneValue: cloneValue,
                    controller: me.controller
                });
                break;
            case "basicAccumulation":
                processor = Ext.create('Mtr.controller.readingtypesgroup.processors.AccumulationProcessor', {
                    cloneValue: cloneValue,
                    controller: me.controller
                });
                break;
            case "basicMeasuringPeriod":
                processor = Ext.create('Mtr.controller.readingtypesgroup.processors.MeasuringPeriodProcessor', {
                    cloneValue: cloneValue,
                    controller: me.controller
                });
                break;
            case "basicMetricMultiplier":
                processor = Ext.create('Mtr.controller.readingtypesgroup.processors.MetricMultiplierProcessor', {
                    cloneValue: cloneValue,
                    controller: me.controller
                });
                break;
            case "basicPhases":
                processor = Ext.create('Mtr.controller.readingtypesgroup.processors.PhasesProcessor', {
                    cloneValue: cloneValue,
                    controller: me.controller
                });
                break;
            case "basicTou":
                processor = Ext.create('Mtr.controller.readingtypesgroup.processors.TimeOfUseProcessor', {
                    cloneValue: cloneValue,
                    controller: me.controller
                });
                break;
            case "basicCpp":
                processor = Ext.create('Mtr.controller.readingtypesgroup.processors.CriticalPeakPeriodProcessor', {
                    cloneValue: cloneValue,
                    controller: me.controller
                });
                break;
            case "basicConsumptionTier":
                processor = Ext.create('Mtr.controller.readingtypesgroup.processors.ConsumptionTierProcessor', {
                    cloneValue: cloneValue,
                    controller: me.controller
                });
                break;
            case "commodity":
                processor = Ext.create('Mtr.controller.readingtypesgroup.processors.CommodityExtendedProcessor', {
                    cloneValue: cloneValue,
                    controller: me.controller
                });
                break;

            case "measurementKind":
                processor = Ext.create('Mtr.controller.readingtypesgroup.processors.KindExtendedProcessor', {
                    cloneValue: cloneValue,
                    controller: me.controller
                });
                break;
            case "flowDirection":
                processor = Ext.create('Mtr.controller.readingtypesgroup.processors.FlowExtendedProcessor', {
                    cloneValue: cloneValue,
                    controller: me.controller
                });
                break;
            case "unit":
                processor = Ext.create('Mtr.controller.readingtypesgroup.processors.UnitExtendedProcessor', {
                    cloneValue: cloneValue,
                    controller: me.controller
                });
                break;

        }

        if (processor) {
            me.processorsMap.add(type, processor);
        }
        return processor;
    }
});