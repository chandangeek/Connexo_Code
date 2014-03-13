Ext.define('Isu.component.filter.view.Filter', {
    extend: 'Ext.form.Panel',
    alias: 'widget.filter-form',

    loadRecord: function(filter) {
        var me = this,
            data = filter.getData(true);

        this.callParent([filter]);
        filter.associations.each(function (association) {
            switch (association.type) {
                case 'hasOne':
                    data[association.name] = me.extractHasOne(filter, association);
                    break;
                case 'hasMany':
                    data[association.name] = me.extractHasMany(filter, association);
                    break;
            }
        });

        return this.getForm().setValues(data);
    },

    extractHasOne: function (filter, association) {
        var name = association.name,
            record = filter.get(name);

        return record ? record.getId() : null;
    },

    extractHasMany: function (filter, association) {
        var name = association.name,
            store = filter[name](),
            result = [];

        store.each(function(record){
            result.push(record.getId());
        });

        return result;
    },

    updateRecord: function (record) {
        this.callParent([record]);
        var me = this,
            values = this.getValues();

        record = record || this._record;
        record.associations.each(function (association) {
            switch (association.type) {
                case 'hasOne':
                    me.hydrateHasOne(record, association, values);
                    break;
                case 'hasMany':
                    me.hydrateHasMany(record, association, values);
                    break;
            }
        });

        return this;
    },

    hydrateHasOne: function (record, association, values) {
        var name = association.name,
            cmp = this.down('[name="' + name + '"]');

        if (cmp && cmp.mixins.bindable && values[name]) {
            var store = cmp.getStore();
            var rec = store.getById(values[name]);
            record.set(name, rec);
        }
    },

    hydrateHasMany: function (record, association, values) {
        var name = association.name,
            store = record[name](),
            cmp = this.down('[name="' + name + '"]');

        if (cmp && cmp.mixins.bindable && values[name]) {
            var cmpStore = cmp.getStore();

            if (!_.isArray(values[name])) {
                values[name] = [values[name]];
            }

            var records = _.map(values[name], function (value) {
                return cmpStore.getById(value);
            });
            store.loadRecords(records, {});
        }

        return this;
    }
});