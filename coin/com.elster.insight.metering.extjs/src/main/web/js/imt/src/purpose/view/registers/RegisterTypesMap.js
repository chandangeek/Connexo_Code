Ext.define('Imt.purpose.view.registers.RegisterTypesMap', {
    singleton: true,

    addEditForms: {
        numerical: 'add-index-register-reading',
        flag: 'add-flag-register-reading',
        billing: 'add-billing-register-reading',
        text: 'add-text-register-reading'
    },

    getAddEditForms: function (key) {
        return this.addEditForms[key] ? this.addEditForms[key] : this.addEditForms['numerical'];
    }

});
