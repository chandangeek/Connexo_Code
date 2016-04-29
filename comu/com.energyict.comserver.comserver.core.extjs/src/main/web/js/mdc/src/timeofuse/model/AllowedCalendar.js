Ext.define('Mdc.timeofuse.model.AllowedCalendar', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.model.timeofuse.Calendar'
    ],
    fields: [
        {name: 'name', type: 'string'},
        {name: 'status', type: 'string'}
    ],

    associations: [
        {
            name: 'calendar',
            type: 'hasOne',
            model: 'Uni.model.timeofuse.Calendar',
            associationKey: 'calendar',
            foreignKey: 'calendar',
            getTypeDiscriminator: function (node) {
                return 'Uni.model.timeofuse.Calendar';
            }
        }
    ]

});
