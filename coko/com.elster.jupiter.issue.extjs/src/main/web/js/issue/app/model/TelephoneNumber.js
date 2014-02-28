Ext.define('Mtr.model.TelephoneNumber', {
    extend: 'Ext.data.Model',
    fields: [
        'areaCode',
        'cityCode',
        'countryCode',
        'extension',
        'localNumber'
    ]
});