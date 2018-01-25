/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mtr.controller.readingtypesgroup.processors.FlowProcessor', {
    extend: 'Mtr.controller.readingtypesgroup.processors.AlwaysVisibleComboProcessor',

    END_DEVICE_CONDITION: 41,

    isDisabled: function () {
        var commodity = this.controller.getBasicCommodity().getValue();
        return commodity === this.NOT_APPLICABLE || commodity === this.END_DEVICE_CONDITION;
    },

    getCombo: function (){
        return this.controller.getBasicFlowDirection();
    }

});
