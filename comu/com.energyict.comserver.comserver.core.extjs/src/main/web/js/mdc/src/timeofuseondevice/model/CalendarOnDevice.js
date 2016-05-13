Ext.define('Mdc.timeofuseondevice.model.CalendarOnDevice', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.model.timeofuse.Calendar'
    ],
    fields: [
        {name: 'passiveCalendars', type: 'auto'}
    ],



    associations: [
        {
            name: 'activeCalendar',
            type: 'hasOne',
            model: 'Uni.model.timeofuse.Calendar',
            getterName: 'getActiveCalendar',
            setterName: 'setActiveCalendar',
            associationKey: 'activeCalendar',
            foreignKey: 'activeCalendar',
            getTypeDiscriminator: function (node) {
                return 'Uni.model.timeofuse.Calendar';
            }
        }
    ]

});
