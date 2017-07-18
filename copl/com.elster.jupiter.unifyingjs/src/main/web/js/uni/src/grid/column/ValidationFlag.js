/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.grid.column.ValidationFlag', {
    extend: 'Ext.grid.column.Column',
    xtype: 'validation-flag-column',
    header: Uni.I18n.translate('device.registerData.value', 'UNI', 'Value'),
    renderer: function (value, metaData, record) {
        if (!Ext.isEmpty(value) && record.get('validationResult')) {
            var status = record.get('validationResult').split('.')[1],
                icon = '';

            if (record.get('isConfirmed')) {
                icon = '<span class="icon-checkmark" style="margin-left:10px; position:absolute"></span>'
            } else if (status === 'suspect') {
                icon = '<span class="icon-flag5" style="margin-left:10px; position:absolute; color:red"></span>';
            } else if (status === 'notValidated') {
                icon = '<span class="icon-flag6" style="margin-left:10px; position:absolute;"></span>';
            }
            return Ext.String.htmlEncode(value) + icon;
        }
        return '-';
    }
});
