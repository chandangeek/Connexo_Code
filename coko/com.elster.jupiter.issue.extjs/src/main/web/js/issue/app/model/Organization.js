Ext.define('Mtr.model.Organization', {
    extend: 'Mtr.model.Party',
    constructor: function () {
        this.callParent(arguments);
        this.associations.push([
            {
                type: 'hasOne',
                model: 'Mtr.model.TelephoneNumber',
                associationKey: 'phone1',
                getterName: 'getPhone1',
                setterName: 'setPhone1'
            },
            {
                type: 'hasOne',
                model: 'Mtr.model.TelephoneNumber',
                associationKey: 'phone2',
                getterName: 'getPhone2',
                setterName: 'setPhone2'
            },
            {
                type: 'hasOne',
                model: 'Mtr.model.PostalAddress',
                associationKey: 'postalAddress'
            },
            {
                type: 'hasOne',
                model: 'Mtr.model.StreetAddress',
                associationKey: 'streetAddress'
            }
        ]);
    }
});