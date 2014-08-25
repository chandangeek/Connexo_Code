Ext.define('Mdc.model.ChannelOfLoadProfilesOfDeviceDataFilter', {
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
        var queryParams;

        this.setIntervalEnd();

        queryParams = this.getData(false);

        delete queryParams.duration;
        delete queryParams.id;

        return queryParams;
    },

    setIntervalEnd: function () {
        var intervalStartDate = new Date(this.get('intervalStart')),
            intervalStartDateTime = intervalStartDate.getTime(),
            duration = this.get('duration'),
            count = duration.get('count'),
            intervalEnd;

        switch (duration.get('timeUnit')) {
            case 'minutes':
                intervalEnd = intervalStartDateTime + count * 60000;
                break;
            case 'hours':
                intervalEnd = intervalStartDateTime + count * 1440000;
                break;
            case 'days':
                intervalEnd = intervalStartDateTime + count * 86400000;
                break;
            case 'weeks':
                intervalEnd = intervalStartDateTime + count * 604800000;
                break;
            case 'months':
                intervalEnd = intervalStartDateTime.setMonth(intervalStartDateTime.getMonth() + count);
                break;
            case 'years':
                intervalEnd = intervalStartDateTime.setFullYear(intervalStartDateTime.getFullYear() + count);
                break;
        }

        return this.set('intervalEnd', intervalEnd);
    }
});