/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.device.form.DeviceDateField', {
    extend: 'Ext.form.field.Display',
    xtype: 'deviceFormDateField',
    fullInfo: false,

    renderer: function (value) {
        if (value && (value.available || this.fullInfo)) {
            this.show();
            if (Ext.isEmpty(value.displayValue)) {
                return '-'
            } else {
                return Uni.DateTime.formatDateTimeShort(new Date(value.displayValue));
            }
        } else {
            this.hide();
            return null;
        }
    }
});



