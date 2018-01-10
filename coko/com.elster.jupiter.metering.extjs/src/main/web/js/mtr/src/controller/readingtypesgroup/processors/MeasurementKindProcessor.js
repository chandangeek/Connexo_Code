/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mtr.controller.readingtypesgroup.processors.MeasurementKindProcessor', {
    extend: 'Mtr.controller.readingtypesgroup.processors.AlwaysVisibleComboProcessor',

     getCombo: function (){
        return this.controller.getBasicMeasurementKind();
    }
});
