Ext.define('Mtr.model.Person', {
    extend: 'Mtr.model.Party',
    fields: [
        'firstName',
        'lastName',
        'mName',
        'prefix',
        'suffix',
        'specialNeed'
    ],
    associations: [
        {
            type: 'hasOne',
            model: 'Mtr.model.TelephoneNumber',
            associationKey: 'landLinePhone',
            getterName: 'getLandLinePhone',
            setterName: 'setLandLinePhone'
        },
        {
            type: 'hasOne',
            model: 'Mtr.model.TelephoneNumber',
            associationKey: 'mobilePhone',
            getterName: 'getMobilePhone',
            setterName: 'setMobilePhone'
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/prt/persons',
        reader: {
            type: 'json',
            root: 'persons'
        }
    }
});