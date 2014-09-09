/**
 * @class Uni.util.Hydrator
 *
 * This is the hydrator which allows you to work with the associations of the Ext.data.model
 */
Ext.define('Uni.util.Hydrator', {

    /**
     * Extracts data from the provided object
     * @param object Ext.data.Model
     * @returns {Object}
     */
    extract: function (object) {
        var me = this,
            data = object.getData();

        object.associations.each(function (association) {
            switch (association.type) {
                case 'hasOne':
                    data[association.name] = me.extractHasOne(object.get(association.name));
                    break;
                case 'hasMany':
                    data[association.name] = me.extractHasMany(object[association.name]());
                    break;
            }
        });

        return data;
    },

    /**
     * Extracts data from the association object to the Integer
     *
     * @param object The associated record
     *
     * @returns {Number}
     */
    extractHasOne: function (object) {
        return object ? object.getId() : null;
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

        store.each(function (record) {
            result.push(record.getId());
        });

        return result;
    },

    /**
     * Hydrates data to the provided object
     *
     * @param data
     * @param object
     */
    hydrate: function (data, object) {
        var me = this,
            fieldData = _.pick(data, object.fields.keys),
            associationData = _.pick(data, object.associations.keys);

        _.each(fieldData, function (item, key) {
            object.set(key, item);
        });

        _.each(associationData, function (item, key) {
            var association = object.associations.get(key);
            switch (association.type) {
                case 'hasOne':
                    if (!object[association.instanceName]) {
                        object[association.instanceName] = Ext.create(association.model);
                    }
                    me.hydrateHasOne(item, object[association.instanceName]);
                    break;
                case 'hasMany':
                    me.hydrateHasMany(item, object[key]());
                    break;
            }
        });

        return object;
    },

    /**
     * Hydrates array data to the associated model field
     *
     * @param data
     * @param object association object
     */
    hydrateHasOne: function (data, object) {
        object.self.load(data);

        return this;
    },

    /**
     * Hydrates array data to the associated model store
     *
     * @param data
     * @param store selected association
     */
    hydrateHasMany: function (data, store) {
        var me = this;
        store.removeAll(); //todo: replace on allowClear property

        if (!data) {
            return this;
        }

        if (!_.isArray(data)) {
            data = [data];
        }

        _.map(data, function (id) {
            if (!_.isObject(id)) {
                var item = {};
                item[store.model.prototype.idProperty] = id;
                id = item;
            }
            var record = store.model.create(id);
            store.add(record);
        });

        return this;
    }
});