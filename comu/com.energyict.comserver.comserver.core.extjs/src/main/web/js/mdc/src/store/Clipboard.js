Ext.define('Mdc.store.Clipboard', {
    extend: 'Ext.data.Store',
    fields: [
        {
            name: 'id',
            type: 'string'
        },
        {
            name: 'value',
            type: 'auto'
        }
    ],

    set: function (name, obj) {
        var model = this.getById(name);

        if (model) {
            model.set('value', obj)
        } else {
            this.add({
                id: name,
                value: obj
            });
        }
    },

    get: function (name) {
        var model = this.getById(name);

        if (model) {
            return model.get('value');
        } else {
            return model;
        }
    },

    clear: function (name) {
        var model = this.getById(name);

        this.remove(model);
    }
});