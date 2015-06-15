Ext.define('Mdc.model.ValidationResultsRegister', {
    extend: 'Ext.data.Model',
	requires: [
        'Uni.property.model.Property'
    ],
    fields: [
        'id',
        'name',
        'interval',
        'intervalEnd',
        'intervalInMs',
        'intervalRecord',
        {
            name: 'intervalStart',
            convert: function (value, record) {
                return value;
            }
        },
		{
            name: 'total',            
            convert: function (value, record) {
                if (value) {
                    return Ext.String.format(Uni.I18n.translate('validationResults.suspects', 'MDC', '{0} suspects'), value);
                }
                return '';
            }
        }
    ]

});
