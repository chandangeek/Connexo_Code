/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mtr.controller.readingtypesgroup.processors.UnitProcessor', {
    extend: 'Mtr.controller.readingtypesgroup.processors.AlwaysVisibleComboProcessor',

    selectAndDisable: false,

    isDisabled: function () {
        var measurementKind = this.controller.getBasicMeasurementKind().getValue();
        return measurementKind === null || measurementKind === this.NOT_APPLICABLE;
    },

    getFilterParam: function () {
        return this.controller.getBasicMeasurementKind().getValue();
    },

    getCombo: function (){
        return this.controller.getBasicUnit();
    }
});
