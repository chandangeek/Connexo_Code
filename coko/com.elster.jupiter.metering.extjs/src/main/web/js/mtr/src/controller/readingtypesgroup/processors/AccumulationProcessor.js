/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mtr.controller.readingtypesgroup.processors.AccumulationProcessor', {
    extend: 'Mtr.controller.readingtypesgroup.processors.AdditionalParamsProcessor',

    isVisible: function () {
        var isMacroPeriodNA = this.controller.getBasicMacroPeriod().getValue() === this.NOT_APPLICABLE,
            isCommodityNA = this.controller.getBasicCommodity().getValue() === this.NOT_APPLICABLE;
        return (isMacroPeriodNA && !isCommodityNA);  // CXO-8254
    },

    getCombo: function (){
        return this.controller.getBasicAccumulation();
    }
});



