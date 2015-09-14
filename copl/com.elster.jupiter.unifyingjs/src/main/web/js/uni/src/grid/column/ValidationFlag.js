Ext.define('Uni.grid.column.ValidationFlag', {
    extend: 'Ext.grid.column.Column',
    xtype: 'validation-flag-column',
    header: Uni.I18n.translate('device.registerData.value', 'UNI', 'Value'),
    renderer: function (value, metaData, record) {
        if (record.get('validationResult')) {
            var result = record.get('validationResult'),
                status = result.split('.')[1],
                cls = 'icon-validation-cell ';
            if (status === 'suspect') {
                cls += 'icon-validation-red'
            }
            if (status === 'notValidated') {
                cls += 'icon-validation-black'
            }
            metaData.tdCls = cls;
            if (!Ext.isEmpty(value)) {
                return record.get('isConfirmed') ? Ext.String.htmlEncode(value) + '<span style="margin: 0 0 0 10px; position: absolute" class="icon-checkmark3"</span>' : Ext.String.htmlEncode(value);
            } else {
                return '';
            }
        }
    }
});
