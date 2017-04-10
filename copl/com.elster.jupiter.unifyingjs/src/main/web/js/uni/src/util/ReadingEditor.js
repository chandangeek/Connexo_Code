/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.util.ReadingEditor', {
    singleton: true,

    valueCorrector: function(value, operationType, correctionAmount){
        var operationMap = {
            'ADD': function(a, b){
                return 1*a + 1*b;
            },
            'SUBTRACT': function(a, b){
                return a - b;
            },
            'MULTIPLY': function(a, b){
                return a * b;
            }
        };

        return operationMap[operationType](value, correctionAmount);
    },

    checkReadingInfoStatus: function (readingInfo) {
        var validationStatus = readingInfo.validationResult ? readingInfo.validationResult.split('.')[1] : '',
            isSuspect = function(){ return validationStatus === 'suspect'}
            ,
            isEstimated = function(){ return readingInfo.estimatedByRule || readingInfo.estimatedNotSaved},
            isSuspectOrEstimated = function(){ return isSuspect() || isEstimated()};
        return {
            isSuspect: isSuspect,
            isEstimated: isEstimated,
            isSuspectOrEstimated: isSuspectOrEstimated
        }
    },

    setReadingInfoStatus: function(reading, status){
        var statusMap = {
            'corrected': {
                isConfirmed: undefined,
                estimatedByRule: undefined,
                estimatedNotSaved: undefined,
                confirmedNotSaved: undefined,
                removedNotSaved: undefined,
                mainModificationState: {
                    flag:'EDITED',
                    date: new Date(),
                    app: null
                },
                modificationState: {
                    flag:'EDITED',
                    date: new Date(),
                    app: null
                }
            }
        };

        // if(readingInfo.isModel){
            reading.set(statusMap[status]);
        // } else {
        //     Ext.merge(readingInfo, statusMap[status]);
        // }

    }

});