Ext.define('Mdc.model.LoadProfilesOfDeviceDataFilter', {
    extend: 'Ext.data.Model',
    requires: [
        'Mdc.model.LoadProfileDataDuration'
    ],
    fields: [
        {name: 'intervalStart', defaultValue: undefined},
        {name: 'intervalEnd', defaultValue: undefined}
    ],

    associations: [
        {
            name: 'duration',
            type: 'hasOne',
            model: 'Mdc.model.LoadProfileDataDuration',
            associationKey: 'duration',
            setterName: 'setDuration',
            getterName: 'getDuration'
        }
    ],

    getFilterQueryParams: function () {
        var duration = this.get('duration'),
            queryParams;

        this.set('intervalEnd', moment(this.get('intervalStart')).add(duration.get('timeUnit'), duration.get('count')).valueOf());

        queryParams = this.getData(false);

        delete queryParams.duration;
        delete queryParams.id;

        return queryParams;
    }
});