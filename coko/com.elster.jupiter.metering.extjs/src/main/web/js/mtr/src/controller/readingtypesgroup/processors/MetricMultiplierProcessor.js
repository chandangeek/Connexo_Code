/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mtr.controller.readingtypesgroup.processors.MetricMultiplierProcessor', {
    extend: 'Mtr.controller.readingtypesgroup.processors.AdditionalParamsProcessor',

    // electricity, gas and water
    commodityValues: [1, 2, 7, 9],

    isVisible: function () {
        var me = this,
            commodity = me.controller.getBasicCommodity().getValue();
        return me.commodityValues.indexOf(commodity) !== -1;
    },

    getCombo: function (){
        return this.controller.getBasicMetricMultiplier();
    }
});

