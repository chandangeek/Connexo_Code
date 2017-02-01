/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceattributes.form.DateFieldEdited', {
    extend: 'Uni.form.field.DateTime',
    xtype: 'deviceDateFieldEdited',
    width: 450,
    editable: false,
    required: true,
    layout: 'hbox',
    dateConfig: {
        width: 140
    },
    hoursConfig: {
        width: 60
    },
    minutesConfig: {
        width: 60
    },

    getTimeStampValue: function() {
        if (this.getValue()) {
            return this.getValue().getTime();
        } else {
            return null;
        }
    }
});



