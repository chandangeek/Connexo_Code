/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Est.main.store.Clipboard', {
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

        if (model) {
            this.remove(model);
        }
    }
});