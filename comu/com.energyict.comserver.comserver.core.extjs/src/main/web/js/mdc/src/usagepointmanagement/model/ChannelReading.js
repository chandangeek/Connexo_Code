/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.usagepointmanagement.model.ChannelReading', {
    extend: 'Uni.model.Version',
    fields: [
        'value', 'interval', 'readingTime', 'readingQualities', 'validationResult', 'dataValidated', 'validationAction', 'validationRules',
        {
            name: 'id',
            mapping: 'interval.end'
        },
        {
            name: 'validation',
            mapping: function (data) {
                var result = 'NOT_VALIDATED';

                if (!data.readingTime && !data.value) {
                    result = 'NO_LINKED_DEVICES';
                } else {
                    switch (data.validationResult) {
                        case 'validationStatus.ok':
                            result = 'OK';
                            break;
                        case 'validationStatus.suspect':
                            switch (data.validationAction.toLowerCase()) {
                                case 'warnOnly':
                                    result = 'INFORMATIVE';
                                    break;
                                case 'fail':
                                    result = 'SUSPECT';
                                    break;
                            }
                            break;
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
    ],
    proxy: {
        type: 'rest',
        url: '/api/upr/usagepoints/{usagePointId}/channels/{channelId}/data',
        reader: {
            type: 'json'
        }
    }
});