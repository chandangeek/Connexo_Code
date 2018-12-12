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

    setState: function (state) {
        this.getCombo().setVisible(state);
    },

    /**
     * If no cloning, store loaded => select NOT_APPLICABLE
     * If no cloning, store not loaded => do nothing
     * If cloning, combo is not visible => select NOT_APPLICABLE
     * If cloning, comb is visible => select clone value
     */
    process: function() {
        var visible = this.isVisible();
        if (this.cloneValue && visible){
            this.setComboValue(true);
        } else {
            this.restoreValue(visible);
        }
    }
});


