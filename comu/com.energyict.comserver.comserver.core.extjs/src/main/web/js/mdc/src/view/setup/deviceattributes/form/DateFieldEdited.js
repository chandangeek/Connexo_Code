/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceattributes.form.DateFieldEdited', {
    extend: 'Ext.form.FieldContainer',
    xtype: 'deviceDateFieldEdited',
    layout: 'hbox',
    initialValue: undefined,
    items: [
         {
            xtype: 'container',
            width: '100%',
            layout: {
                 type: 'hbox',
                 align: 'left'
            },
            items: [
            {
                xtype: 'date-time',
                layout: 'hbox',
                editable: false,
                required: false,
                width: 285,
                dateConfig: {
                    width: 140
                },
                hoursConfig: {
                    width: 60
                },
                minutesConfig: {
                    width: 60
                },
                listeners: {
                     change: {
                          fn : function (field) {
                               if (!this.resetButton) this.resetButton = this.nextSibling('uni-default-button');
                               if (field){
                                   if (field.getValue() && field.getValue().getTime() ){
                                        this.resetButton.setDisabled(false);
                                   }else{
                                        this.resetButton.setDisabled(true);
                                   }
                               }
                          }
                     }
                }
            },
            {
                 xtype: 'uni-default-button',
                 disabled: true,
                 tooltip: Uni.I18n.translate('general.restoreDefaultEmptyValue', 'MDC', 'Restore to default empty value'),
                 hidden: false,
                 handler: function() {
                      this.previousSibling('date-time').setValue();
                      this.setDisabled(true);
                 }
            }
         ],
    }],

    getTimeStampValue: function() {
        var dateTimeWidget = this.down('date-time');
        if (dateTimeWidget && dateTimeWidget.getValue && dateTimeWidget.getValue()) {
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

    setInitialValue: function(value){
        this.initialValue = value;
        this.setValue(value);
    },

    setValue: function(value){
        var dateTimeWidget = this.down('date-time');
        if (dateTimeWidget && dateTimeWidget.setValue) {
            dateTimeWidget.setValue(value);
        }
    },

    markInvalid: function(value){
        var dateTimeWidget = this.down('date-time');
        if (dateTimeWidget) {
            dateTimeWidget.markInvalid(value);
        }
    }
});



