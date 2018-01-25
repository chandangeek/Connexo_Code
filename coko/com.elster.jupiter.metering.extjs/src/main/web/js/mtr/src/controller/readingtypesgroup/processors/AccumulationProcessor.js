/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mtr.controller.readingtypesgroup.processors.AccumulationProcessor', {
    extend: 'Mtr.controller.readingtypesgroup.processors.AdditionalParamsProcessor',

    /**
     * Visible when commodity is set and period is not applicable
     * @returns {boolean}
     */
    isVisible: function () {
        return !this.controller.getBasicMacroPeriod().getValue()
            && this.controller.getBasicCommodity().getValue() !== this.NOT_APPLICABLE;
    },

    getCombo: function (){
        return this.controller.getBasicAccumulation();
    }
});



