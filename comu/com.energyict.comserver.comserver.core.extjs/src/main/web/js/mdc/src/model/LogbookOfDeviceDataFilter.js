Ext.define('Mdc.model.LogbookOfDeviceDataFilter', {
    extend: 'Uni.component.filter.model.Filter',
    requires: [
        'Mdc.model.Domain',
        'Mdc.model.Subdomain',
        'Mdc.model.EventOrAction'
    ],
    fields: [
        'intervalStart',
        'intervalEnd'
    ],
    associations: [
        {
            name: 'domain',
            type: 'hasOne',
            model: 'Mdc.model.Domain',
            associationKey: 'domain',
            setterName: 'setDomain',
            getterName: 'getDomain'
        },
        {
            name: 'subDomain',
            type: 'hasOne',
            model: 'Mdc.model.Subdomain',
            associationKey: 'subDomain',
            setterName: 'setSubDomain',
            getterName: 'getSubDomain'
        },
        {
            name: 'eventOrAction',
            type: 'hasOne',
            model: 'Mdc.model.EventOrAction',
            associationKey: 'eventOrAction',
            setterName: 'setEventOrAction',
            getterName: 'getEventOrAction'
        }
    ],

    getFilterQueryParams: function () {
        var queryParams = [];

        Ext.iterate(this.getPlainData(), function (key, value) {
            if (Ext.isDate(value)) {
                if (key === 'intervalEnd') {
                    value = moment(value).endOf('day').toDate();
                }
                value = value.getTime();
            }
            queryParams.push({
                property: key,
                value: value
            });
        });

        return Ext.JSON.encode(queryParams);
    }
});