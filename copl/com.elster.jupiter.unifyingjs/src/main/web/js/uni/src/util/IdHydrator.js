/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.util.IdHydrator
 *
 * This is the hydrator which allows you to work with the associations of the Ext.data.model
 */
Ext.define('Uni.util.IdHydrator', {

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
            associationRawData = _.pick(data, object.associations.keys),
            associatedData = {};

        // set object fields
        _.each(fieldData, function (item, key) {
            object.set(key, item);
        });

        // set object associations
        _.each(associationRawData, function (item, key) {
            var association = object.associations.get(key),
                idProperty = association.associatedModel.prototype.idProperty;

            switch (association.type) {
                case 'hasOne':
                    associatedData[key] = me.itemToData(item, idProperty);
                    break;
                case 'hasMany':
                    associatedData[key] = me.itemsToData(item, idProperty);
                    break;
            }
        });

        object.getProxy().getReader().readAssociated(object, associatedData);
    },

    /**
     * Transforms id to model data
     *
     * @param item
     * @param idProperty
     * @returns {*}
     */
    itemToData: function (item, idProperty) {
        if (item instanceof Ext.data.Model) {
            return item;
        } else {
            var data = {};
            data[idProperty] = item;
            return data;
        }
    },

    /**
     * Transforms ids to model data
     *
     * @param data
     * @param idProperty
     */
    itemsToData: function (data, idProperty) {
        var me  = this;

        if (!data) {
            return false;
        }

        if (!_.isArray(data)) {
            data = [data];
        }

        return  _.map(data, function (item) {
            return me.itemToData(item, idProperty);
        });
    }
});