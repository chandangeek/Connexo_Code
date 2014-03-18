Ext.define('Isu.component.filter.view.Filter', {
    extend: 'Ext.form.Panel',
    alias: 'widget.filter-form',

    /**
     * @override
     *
     * Load data to the form of the model with associations.
     *
     * @param filter Isu.component.filter.model.Filter
     * @returns {Ext.form.Basic}
     */
    loadRecord: function(filter) {
        var me = this,
            data = filter.getData(true);

        this.callParent([filter]);
        filter.associations.each(function (association) {
            switch (association.type) {
                case 'hasOne':
                    data[association.name] = me.extractHasOne(filter.get(association.name));
                    break;
                case 'hasMany':
                    data[association.name] = me.extractHasMany(filter[association.name]());
                    break;
            }
        });

        return this.getForm().setValues(data);
    },

    /**
     * Extracts data from the association object to the Integer
     *
     * @param record The associated record
     *
     * @returns {Number}
     */
    extractHasOne: function (record) {
        return record ? record.getId() : null;
    },

    /**
     * Extracts data from the store to the array
     *
     * @param store The associated store
     *
     * @returns {Number[]}
     */
    extractHasMany: function (store) {
        var result = [];

        store.each(function(record){
            result.push(record.getId());
        });

        return result;
    },

    /**
     * @override
     *
     * Extracts data from the form and bind to the filter model
     *
     * @param record The associated filter record
     * @returns {Isu.component.filter.view.Filter}
     */
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

    /**
     * Hydrates array data to the associated model field
     *
     * @param record Filter model
     * @param association selected association
     * @param values data array
     */
    hydrateHasOne: function (record, association, values) {
        var name = association.name,
            cmp = this.down('[name="' + name + '"]');

        if (!values[name]) {
            record.set(name, null);
        } else if (cmp && cmp.mixins.bindable) {
            var store = cmp.getStore();
            var rec = store.getById(values[name]);
            record.set(name, rec);
        }

        return this;
    },

    /**
     * Hydrates array data to the associated model store
     *
     * @param record Filter model
     * @param association selected association
     * @param values data array
     */
    hydrateHasMany: function (record, association, values) {
        var name = association.name,
            store = record[name](),
            cmp = this.down('[name="' + name + '"]');

        if (!values[name]) {
            store.removeAll();

        } else if (cmp && cmp.mixins.bindable && values[name]) {
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