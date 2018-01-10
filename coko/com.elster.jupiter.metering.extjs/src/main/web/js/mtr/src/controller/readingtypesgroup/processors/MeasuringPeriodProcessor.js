/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mtr.controller.readingtypesgroup.processors.MeasuringPeriodProcessor', {
    extend: 'Mtr.controller.readingtypesgroup.processors.AdditionalParamsProcessor',

    LESS_THAN_DAY_INTERVAL: 0x10000,

    isVisible: function () {
        return this.controller.getBasicMacroPeriod().getValue() === this.LESS_THAN_DAY_INTERVAL;
    },

    getCombo: function (){
        return this.controller.getBasicMeasuringPeriod();
    }

});

