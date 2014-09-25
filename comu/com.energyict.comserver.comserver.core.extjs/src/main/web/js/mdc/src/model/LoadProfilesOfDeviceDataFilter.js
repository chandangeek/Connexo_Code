Ext.define('Mdc.model.LoadProfilesOfDeviceDataFilter', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.data.proxy.QueryStringProxy',
        'Mdc.model.LoadProfileDataDuration',
        'Mdc.model.DateRange'
    ],
    proxy: {
        type: 'querystring',
        root: 'filter'
    },
    fields: [
        {name: 'intervalStart', defaultValue: undefined},
        {name: 'intervalEnd', defaultValue: undefined}
    ],

    associations: [
        {
            type: 'hasOne',
            name: 'endOfInterval',
            model: 'Mdc.model.DateRange',
            instanceName: 'endOfInterval',
            associationKey: 'endOfInterval',
            getterName: 'getEndOfInterval',
            setterName: 'setEndOfInterval'
        },
        {
            type: 'hasOne',
            name: 'duration',
            model: 'Mdc.model.LoadProfileDataDuration',
            associationKey: 'duration',
            getterName: 'getDuration',
            setterName: 'setDuration'
        }
    ],

    getFilterQueryParams: function () {
        var duration = this.getDuration(),
            intervalStart = this.get('intervalStart'),
            queryParams;

        this.set('intervalEnd', moment(intervalStart).add(duration.get('timeUnit'), duration.get('count')).valueOf());

        queryParams = this.getData(false);

        delete queryParams.duration;
        delete queryParams.id;

        return queryParams;
    }
});