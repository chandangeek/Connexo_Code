/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mtr.controller.readingtypesgroup.processors.UnitProcessor', {
    extend: 'Mtr.controller.readingtypesgroup.processors.ComboProcessor',

    disabledForLoad: false,
    isDisabled: function (measurementKind) {
        return measurementKind === null || measurementKind === this.NOT_APPLICABLE;
    },

    process: function() {
        var me = this,
            combo = me.getCombo(),
            measurementKind = me.controller.getBasicMeasurementKind().getValue(),
            disabled = me.isDisabled(measurementKind);

        combo.setDisabled(disabled || this.disabledForLoad);
        me.resetComboValue();
        if (!disabled) {
            combo.getStore().getProxy().setExtraParam('filter', measurementKind);
            me.setComboValue();
        }
    },

    getCombo: function (){
        return this.controller.getBasicUnit();
    }
});
