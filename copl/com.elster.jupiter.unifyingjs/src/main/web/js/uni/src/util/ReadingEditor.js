/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.util.ReadingEditor', {
    singleton: true,

    checkReadingInfoStatus: function (readingInfo) {
        var validationStatus = readingInfo.validationResult ? readingInfo.validationResult.split('.')[1] : '',
            isSuspect = function(){ return validationStatus === 'suspect'},
            isEstimated = function(){ return readingInfo.estimatedByRule },
            isSuspectOrEstimated = function(){ return isSuspect() || isEstimated()};
        return {
            isSuspect: isSuspect,
            isEstimated: isEstimated,
            isSuspectOrEstimated: isSuspectOrEstimated
        }
    },

    modificationState: function(status){
        return {
            flag: status,
            date: null,
            app: null
        }
    }

});