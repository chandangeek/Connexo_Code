Ext.define('Imt.purpose.model.Reading', {
    extend: 'Uni.model.Version',
    requires: [],
    fields: [
        {name: 'value', type: 'auto', useNull: true},
        {name: 'interval', type: 'auto', useNull: true},
        {name: 'readingTime', dateFormat: 'time', type: 'date'},
        {name: 'dataValidated', type: 'auto'},
        {name: 'action', type: 'auto'},
        {name: 'validationResult', type: 'auto'},
        {name: 'validationRules', type: 'auto'},
        'plotband',
        {
            name: 'readingProperties',
            persist: false,
            mapping: function (data) {
                var result = {},
                    validationResult = data.validationResult.split('.')[1];
                
                if (data.validationResult) {
                    result.suspect = validationResult == 'suspect';
                    validationResult == 'notValidated' ? result.notValidated = true : result.notValidated = false;

                    if (data.action == 'FAIL') {
                        result.suspect = true;
                        result['informative'] = false;
                    }
                    if (data.action == 'WARN_ONLY') {
                        result.suspect ? result['informative'] = false : result['informative'] = true;
                    }
                }
                
                return result;                
            }
        },
        {
            name: 'interval_end',
            persist: false,
            mapping: 'interval.end',
            dateFormat: 'time',
            type: 'date'
        }
    ]
});