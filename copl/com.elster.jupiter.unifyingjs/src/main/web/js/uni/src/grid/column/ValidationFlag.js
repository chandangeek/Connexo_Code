Ext.define('Uni.grid.column.ValidationFlag', {
    extend: 'Ext.grid.column.Column',
    xtype: 'validation-flag-column',
    header: Uni.I18n.translate('device.registerData.value', 'MDC', 'Value'),

    renderer: function (value, metaData, record) {
        switch (record.get('validationResult')) {
            case 'validationStatus.notValidated':
                return '<span class="validation-column-align"><span class="icon-validation icon-validation-black"></span>';
                break;
            case 'validationStatus.ok':
                return '<span class="validation-column-align"><span class="icon-validation"></span>';
                break;
            case 'validationStatus.suspect':
                return '<span class="validation-column-align"><span class="icon-validation icon-validation-red"></span>';
                break;
            default:
                return '';
                break;
        }
    }
});
