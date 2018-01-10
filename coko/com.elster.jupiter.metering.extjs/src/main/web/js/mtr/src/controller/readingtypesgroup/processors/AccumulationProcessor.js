/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mtr.controller.readingtypesgroup.processors.AccumulationProcessor', {
    extend: 'Mtr.controller.readingtypesgroup.processors.AdditionalParamsProcessor',

    isVisible: function () {
        return this.controller.getBasicMacroPeriod().getValue() === this.NOT_APPLICABLE;
    },

    getCombo: function (){
        return this.controller.getBasicAccumulation();
    }
});



