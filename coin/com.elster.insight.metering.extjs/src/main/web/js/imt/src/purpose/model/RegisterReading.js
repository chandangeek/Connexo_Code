Ext.define('Imt.purpose.model.RegisterReading', {
    extend: 'Ext.data.Model',
    idProperty: 'timeStamp',
    requires: [],
    fields: [
        {name: 'value', type: 'auto', useNull: true},
        {name: 'timeStamp', type: 'auto', useNull: true},
        {name: 'type', type: 'auto'},
        {name: 'validationResult', type: 'auto', useNull: true, persist: false},
        {name: 'dataValidated', type: 'auto'},
        {name: 'validationRules', type: 'auto', useNull: true, persist: false},
        {name: 'isConfirmed', type: 'auto'},
        {name: 'calculatedValue', type: 'auto'},
        {name: 'reportedDateTime', type: 'auto', defaultValue: null, useNull: true},
        {name: 'interval', type:'auto', useNull: true},
        {
            name: 'interval.end',
            defaultValue: null,
            useNull: true
        },
        {
            name: 'interval.start',
            defaultValue: null,
            useNull: true
        },
        {name: 'confirmedNotSaved', type: 'auto', useNull: true, persist: false},
        {
            name: 'readingProperties',
            persist: false,
            mapping: function (data) {
                var result = {},
                    validationResult;

                if(data.validationResult){
                    validationResult = data.validationResult.split('.')[1]
                }

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
            name: 'modificationState',
            persist: false,
            mapping: function (data) {
                var result = null;

                if (data.modificationFlag && data.modificationDate) {
                    result = {
                        flag: data.modificationFlag,
                        date: data.modificationDate,
                        app: data.editedInApp
                    }
                }
                return result;
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{usagePointId}/purposes/{purposeId}/outputs/{outputId}/registerData',
        timeout: 300000
    }
});