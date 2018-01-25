/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.controller.readingtypesgroup.processors.ExtendedFieldsProcessor', {
    extend: 'Mtr.controller.readingtypesgroup.processors.ComboProcessor',

    selectAndDisable: false,

    setState: function (state) {
        this.getCombo().setDisabled(state);
    },

    process: function () {
        if (this.selectAndDisable) {
            this.setComboValue(true);
        }
    }
});
