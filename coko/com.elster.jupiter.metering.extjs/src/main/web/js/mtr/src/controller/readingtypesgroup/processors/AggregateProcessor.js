/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mtr.controller.readingtypesgroup.processors.AggregateProcessor', {
    extend: 'Mtr.controller.readingtypesgroup.processors.AlwaysVisibleComboProcessor',

    getCombo: function (){
        return this.controller.getBasicAggregate();
    },

    process: function(){
        var me = this,
            commodity = me.controller.getBasicCommodity().getValue(),
            disabled = me.isDisabled(commodity);

        me.getCombo().setDisabled(disabled);
        me.resetComboValue();
        if (!disabled && me.cloneValue) {
            me.setComboValue();
        }
    }
});
