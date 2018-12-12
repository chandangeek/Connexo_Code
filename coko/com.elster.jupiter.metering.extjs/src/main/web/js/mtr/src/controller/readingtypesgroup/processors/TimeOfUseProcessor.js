/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mtr.controller.readingtypesgroup.processors.TimeOfUseProcessor', {
    extend: 'Mtr.controller.readingtypesgroup.processors.AdditionalParamsProcessor',

    getCombo: function (){
        return this.controller.getBasicTimeOfUse();
    }
});
