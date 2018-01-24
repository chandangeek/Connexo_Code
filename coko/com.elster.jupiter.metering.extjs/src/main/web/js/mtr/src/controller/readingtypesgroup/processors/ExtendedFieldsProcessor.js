/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.controller.readingtypesgroup.processors.ExtendedFieldsProcessor', {
    extend: 'Mtr.controller.readingtypesgroup.processors.ComboProcessor',

    disabledForLoad: false,

    process: function () {
        var me = this,
            combo = me.getCombo();

        combo.setDisabled(this.disabledForLoad);
        //combo.setReadOnly(this.disabledForLoad);
        me.resetComboValue();
        me.setComboValue();
    }
});
