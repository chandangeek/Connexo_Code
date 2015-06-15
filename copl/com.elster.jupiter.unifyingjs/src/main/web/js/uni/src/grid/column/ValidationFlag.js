Ext.define('Uni.grid.column.ValidationFlag', {
    extend: 'Ext.grid.column.Column',
    xtype: 'validation-flag-column',
    header: Uni.I18n.translate('device.registerData.value', 'MDC', 'Value'),
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
            return !Ext.isEmpty(value) ? Ext.String.htmlEncode(value) : '';
        }
    }
});
