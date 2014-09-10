/**
 * @class Uni.util.Hydrator
 * todo: rename on
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

    // todo: replace on normal promises
    Promise: function(){
        return {
            callback: null,
            callbacks: [],
            when: function(callbacks) {
                this.callbacks = callbacks;
                return this;
            },
            then: function (callback) {
                this.callbacks.length ? this.callback = callback : callback();
                return this;
            },
            resolve: function(fn) {
                var i = _.indexOf(this.callbacks, fn);
                this.callbacks.splice(i, 1);

                if (!this.callbacks.length) {
                    this.callback.call();
                }
                return this;
            }
        }
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

        // set object fields
        _.each(fieldData, function (item, key) {
            object.set(key, item);
        });

        var promise = new this.Promise();
        var callbacks = [];

        // set object associations
        _.each(associationData, function (item, key) {
            var association = object.associations.get(key);
            var callback = function() {
                promise.resolve(callback);
            };
            callbacks.push(callback);
            switch (association.type) {
                case 'hasOne':
                    object.set(association.foreignKey, item);
                    var getter = association.createGetter();
                    getter.call(object, callback);
                    break;
                case 'hasMany':
                    me.hydrateHasMany(item, object[key]()).then(callback);
                    break;
            }
        });

        // promise replace here
        return promise.when(callbacks);
    },

    /**
     * Hydrates array data to the associated model store
     *
     * @param data
     * @param store selected association
     */
    hydrateHasMany: function (data, store) {
        store.removeAll(); //todo: replace on allowClear property

        if (!data) {
            return this;
        }

        if (!_.isArray(data)) {
            data = [data];
        }

        var promise = new this.Promise();
        var callbacks = [];

        _.map(data, function (id) {
            var callback = function(record) {
                if (status) {
                    store.add(record);
                }
                promise.resolve(callback);
            };

            callbacks.push(callback);
            store.model.load(id, {
                callback: callback
            });
        });

        // promise replace here
        return promise.when(callbacks);
    }
});