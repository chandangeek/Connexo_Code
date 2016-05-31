/**
 * @class Uni.model.timeofuse.Calendar
 */
Ext.define('Uni.model.timeofuse.Calendar', {
    extend: 'Ext.data.Model',

    requires: [
        'Uni.model.timeofuse.Event',
        'Uni.model.timeofuse.DayType',
        'Uni.model.timeofuse.Period',
        'Uni.model.timeofuse.DaysPerType'
    ],


    fields: [
        {name: 'name', type: 'string'},
        {name: 'category', type: 'string'},
        {name: 'mRID', type: 'string'},
        {name: 'id', type: 'number'},
        {name: 'description', type: 'string'},
        {name: 'timeZone', type: 'string'},
        {name: 'startYear', type: 'number'},
        {name: 'weekTemplate', type: 'auto', persist: false},
        {name: 'inUse', type: 'boolean', default: true}
    ],

    associations: [
        {
            name: 'events',
            type: 'hasMany',
            model: 'Uni.model.timeofuse.Event',
            associationKey: 'events',
            foreignKey: 'events',
            getTypeDiscriminator: function (node) {
                return 'Uni.model.timeofuse.Event';
            }
        },
        {
            name: 'dayTypes',
            type: 'hasMany',
            model: 'Uni.model.timeofuse.DayType',
            associationKey: 'dayTypes',
            foreignKey: 'dayTypes',
            getTypeDiscriminator: function (node) {
                return 'Uni.model.timeofuse.DayType';
            }
        },
        {
            name: 'periods',
            type: 'hasMany',
            model: 'Uni.model.timeofuse.Period',
            associationKey: 'periods',
            foreignKey: 'periods',
            getTypeDiscriminator: function (node) {
                return 'Uni.model.timeofuse.Period';
            }
        },
        {
            name: 'daysPerType',
            type: 'hasMany',
            model: 'Uni.model.timeofuse.DaysPerType',
            associationKey: 'daysPerType',
            foreignKey: 'daysPerType',
            getTypeDiscriminator: function (node) {
                return 'Uni.model.timofuse.DaysPerType';
            }
        }
    ],

    proxy: {
        type: 'rest',
        timeout: 120000,
        reader: {
            type: 'json'
        },

        setUrl: function(url) {
            this.url = url;
        }
    }
});