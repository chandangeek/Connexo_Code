/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.model.Reading', {
    extend: 'Uni.model.Version',
    requires: [],
    fields: [
        {name: 'value', type: 'auto', useNull: true},
        {name: 'interval', type: 'auto', useNull: true},
        {name: 'reportedDateTime', dateFormat: 'time', type: 'date'},
        {name: 'dataValidated', type: 'auto'},
        {name: 'action', type: 'auto'},
        {name: 'validationResult', type: 'auto'},
        {name: 'validationRules', type: 'auto'},
        {name: 'confirmedNotSaved', type: 'auto'},
        {name: 'estimatedNotSaved', type: 'auto'},
        {name: 'commentId', type: 'int'},
        {name: 'commentValue', type: 'auto'},
        {name: 'removedNotSaved', type: 'auto'},
        {name: 'confirmed', type: 'auto'},
        {name: 'estimatedByRule', type: 'auto'},
        {name: 'calculatedValue', type: 'auto'},
        {name: 'isConfirmed', type: 'auto'},
        {name: 'isProjected', type: 'auto'},
        {name: 'ruleId', type: 'auto'},
        {name: 'estimatedByRule', type: 'auto'},
        {name: 'readingQualities', type: 'auto', defaultValue: null},
        {name: 'modificationFlag', type: 'auto'},
        {name: 'modificationDate', type: 'auto'},
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
        },
        {
            name: 'mainValidationInfo',
            persist: false,
            mapping: function (data) {
                return {
                    estimatedByRule: data.estimatedByRule,
                    isConfirmed: data.isConfirmed,
                    validationResult: data.validationResult,
                    ruleId: data.ruleId,
                }
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