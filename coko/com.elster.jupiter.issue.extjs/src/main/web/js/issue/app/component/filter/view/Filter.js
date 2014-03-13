Ext.define('Isu.component.filter.view.Filter', {
    extend: 'Ext.form.Panel',
    alias: 'widget.filter-form',

    updateRecord: function (record) {
        this.callParent(record);
        var me = this,
            values = this.getValues();

        record = record || this._record;
        record.associations.each(function (association) {
            switch (association.type) {
                case 'hasOne':
                    me.updateHasOne(record, association, values);
                    break;
                case 'hasMany':
                    me.updateHasMany(record, association, values);
                    break;
            }
        });

        return this;
    },

    updateHasOne: function (record, association, values) {
        var name = association.name,
            cmp = this.down('[name="' + name + '"]');

        if (cmp && cmp.mixins.bindable && values[name]) {
            var store = cmp.getStore();
            var rec = store.getById(values[name]);
            record.set(name, rec);
        }
    },

    updateHasMany: function (record, association, values) {
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