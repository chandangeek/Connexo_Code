/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.util.Hydrator
 * todo: rename on Uni.util.lazyHydrator
 *
 * This is the hydrator which allows you to work with the associations of the Ext.data.model
 */
Ext.define('Uni.util.Hydrator', {
    extend: 'Uni.util.IdHydrator',

    // todo: replace on normal promises
    Promise: function () {
        return {
            callback: null,
            callbacks: [],
            when: function (callbacks) {
                this.callbacks = callbacks;
                return this;
            },
            then: function (callback) {
                this.callbacks.length ? this.callback = callback : callback();
                return this;
            },
            resolve: function (fn) {
                var i = _.indexOf(this.callbacks, fn);
                this.callbacks.splice(i, 1);

                if (!this.callbacks.length) {
                    this.callback && this.callback.call();
                }
                return this;
            }
        };
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
            if(id instanceof Ext.data.Model){
                store.add(id);
            }
            else {
                var callback = function (record) {
                    if (record) {
                        store.add(record);
                    }
                    promise.resolve(callback);
                };

                callbacks.push(callback);
                store.model.load(id, {
                    callback: callback
                });
            }
        });

        // promise replace here
        return promise.when(callbacks);
    }
});