Ext.define('Mdc.timeofuseondevice.model.CalendarOnDevice', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.model.timeofuse.Calendar'
    ],
    fields: [
        {name: 'passiveCalendars', type: 'auto', defaultValue: null},
        {name: 'lastVerified', type: 'number', defaultValue: null}
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
