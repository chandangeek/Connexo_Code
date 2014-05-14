/**
 * @class Uni.util.Hydrator
 *
 * This is the hydrator which allows you to work with the associations of the Ext.data.model
 */
Ext.define('Uni.util.Hydrator', {
    lazyLoading: true,

    /**
     * Extracts data from the provided object
     * @param object Ext.data.Model
     * @returns {Object}
     */
    extract: function(object) {
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

        store.each(function(record){
            result.push(record.getId());
        });

        return result;
    },

    /**
     * Hydrates data to the provided object
     *
     * todo: replace on promises
     *
     * @param data
     * @param object
     * @param callback
     */
    hydrate: function(data, object, callback) {
        var me = this,
            fieldData = _.pick(data, object.fields.keys),
            associationData = _.pick(data, object.associations.keys);

        _.each(fieldData, function(item, key) {
            object.set(key, item);
        });

        var count = _.keys(associationData).length;
        var complete = function() {
            if (--count == 0) {
                callback(object);
            }
        };

        _.each(associationData, function(item, key) {
            var association = object.associations.get(key);
            switch (association.type) {
                case 'hasOne':
                    me.hydrateHasOne(item, object.get(key) || Ext.create(association.model), function(record, operation, success) {
                        object.set(key, record);
                        complete();
                    });
                    break;
                case 'hasMany':
                    me.hydrateHasMany(item, object[key](), function(){
                        complete();
                    });
                    break;
            }
        });
    },

    /**
     * Hydrates array data to the associated model field
     *
     * @param data
     * @param object association object
     * @param callback
     */
    hydrateHasOne: function (data, object, callback) {
        if (!data) {
            callback(null, null, false);
            return this;
        }

        object.self.load(data, {
            callback: callback
        });

        return this;
    },

    /**
     * Hydrates array data to the associated model store
     *
     * @param data
     * @param store selected association
     * @param callback
     */
    hydrateHasMany: function (data, store, callback) {
        var me = this;
        store.removeAll(); //todo: replace on allowClear property

        if (!data) {
            return this;
        }

        if (!_.isArray(data)) {
            data = [data];
        }

        var count = data.length;
        var records = _.map(data, function (id) {
            if (me.lazyLoading) {
                store.model.load(id, {
                    callback: function(record, operation, status) {
                        if (status) {
                            store.add(record);
                        }
                        if (--count == 0) {
                            callback();
                        }
                    }
                });
            } else {
                if (!_.isObject(id)) {
                    var item = {};
                    item[store.model.prototype.idProperty] = id;
                    id = item;
                }
                var record = store.model.create(id);
                store.add(record);
            }
        });

        return this;
    }
});