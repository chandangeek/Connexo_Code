/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mtr.controller.readingtypesgroup.processors.AdditionalParamsProcessor', {
    extend: 'Mtr.controller.readingtypesgroup.processors.ComboProcessor',

    isVisible: function () {
        var me = this,
            commodity = me.controller.getBasicCommodity().getValue();
        return (commodity === me.SECONDARY_ELECTRICITY) || (commodity === me.PRIMARY_ELECTRICITY);
    },

    process: function() {
        var me = this,
            visible = me.isVisible();

        me.getCombo().setVisible(visible);
        me.resetComboValue();
        if (visible && me.cloneValue){
            me.setComboValue();
        }
    }
});


