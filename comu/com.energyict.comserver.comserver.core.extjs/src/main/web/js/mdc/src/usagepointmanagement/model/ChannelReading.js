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
        }
    ],
    proxy: {
        type: 'rest',
        urlTpl: '/api/upr/usagepoints/{mRID}/channels/{channelId}/data',
        reader: {
            type: 'json'
        },
        setUrl: function (mRID, channelId) {
            this.url = this.urlTpl.replace('{mRID}', mRID)
                .replace('{channelId}', channelId);
        }
    }
});