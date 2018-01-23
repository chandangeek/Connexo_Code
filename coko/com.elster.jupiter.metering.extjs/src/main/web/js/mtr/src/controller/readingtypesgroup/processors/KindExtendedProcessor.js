/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.controller.readingtypesgroup.processors.KindExtendedProcessor', {
    extend: 'Mtr.controller.readingtypesgroup.processors.ExtendedFieldsProcessor',

    getCombo: function () {
        return this.controller.getExtendedKind();
    }
});
