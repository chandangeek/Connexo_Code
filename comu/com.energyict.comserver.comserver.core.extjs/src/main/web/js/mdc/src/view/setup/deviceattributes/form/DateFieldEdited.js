/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceattributes.form.DateFieldEdited', {
    extend: 'Ext.form.FieldContainer',
    xtype: 'deviceDateFieldEdited',
    items: [{
        xtype: 'date-time',
        layout: 'hbox',
        editable: false,
        required: false,
        width: 450,
        dateConfig: {
            width: 140
        },
        hoursConfig: {
            width: 60
        },
        minutesConfig: {
            width: 60
        },
    }],

    getTimeStampValue: function() {
        var dateTimeWidget = this.down('date-time');
        if (dateTimeWidget && dateTimeWidget.getValue) {
            return dateTimeWidget.getValue().getTime();
        } else {
            return null;
        }
    },

    getValue: function(){
        var dateTimeWidget = this.down('date-time');
        if (dateTimeWidget && dateTimeWidget.getValue) {
            return dateTimeWidget.getValue();
        } else {
            return null;
        }
    },

    setValue: function(value){
        debugger;
        var dateTimeWidget = this.down('date-time');
        if (dateTimeWidget && dateTimeWidget.setValue) {
            dateTimeWidget.setValue(value);
        }
    }
});



