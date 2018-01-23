/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mtr.controller.readingtypesgroup.processors.AlwaysVisibleComboProcessor', {
    extend: 'Mtr.controller.readingtypesgroup.processors.ComboProcessor',
    disabledForLoad: false,
    isDisabled: function (commodity){
        return commodity === this.NOT_APPLICABLE;
    },

    process: function (value) {
        var me = this,
            combo = me.getCombo(),
            commodity = value || me.controller.getBasicCommodity().getValue(),
            disabled = me.isDisabled(commodity);

        combo.setDisabled(disabled || this.disabledForLoad);
        me.resetComboValue();
        if (!disabled){
            combo.getStore().getProxy().setExtraParam('filter', commodity);
            me.setComboValue();
        }
    }
});
