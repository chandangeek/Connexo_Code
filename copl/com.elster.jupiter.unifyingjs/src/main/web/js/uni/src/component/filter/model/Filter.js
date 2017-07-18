/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.component.filter.model.Filter
 * @deprecated
 *
 * @use Uni.data.proxy.QueryStringProxy together with Uni.util.Hydrator instead
 *
 * Filter model extends Ext.data.Model.
 * Model allows you to retrieve model data as plain object {one level key-value pair}.
 *
 */
Ext.define('Uni.component.filter.model.Filter', {
    extend: 'Ext.data.Model',

    /**
     * Returns array of fields and association names
     *
     * @returns {String[]}
     */
    getFields: function() {
        var fields = [];
        this.fields.each(function(field){
            fields.push(field.name);
        });

        this.associations.each(function(association){
            fields.push(association.name);
        });

        return fields;
    },

    /**
     * Returns plain object with the associated data
     *
     * @returns {Object}
     */
    getPlainData: function() {
        var me = this,
            data = this.getData(true);

        this.associations.each(function (association) {
            switch (association.type) {
                case 'hasOne':
                    data[association.name] = me.extractHasOne(me[association.getterName](), association);
                    break;
                case 'hasMany':
                    data[association.name] = me.extractHasMany(me[association.name](), association);
                    break;
            }
        });

        // filter out empty values
        _.each(data, function(elm, key){
            if (!elm) {
                delete data[key];
            }
        });

        return data;
    },

    /**
     * Extracts data from the association object to the Integer
     *
     * @param record The associated record
     *
     * @returns {Number}
     */
    extractHasOne: function(record) {
        return record ? record.getId() : false;
    },

    /**
     * Extracts data from the store to the array
     *
     * @param store The associated store
     *
     * @returns {Number[]}
     */
    extractHasMany: function(store) {
        var result = [];
        store.each(function(record){
            result.push(record.getId());
        });

        return result;
    },

    removeFilterParam: function(key, id) {
        if (id) {
            var store = this[key]();
            var rec = store.getById(id);
            if (rec) {
                store.remove(rec);
            }
        } else if (!_.isUndefined(this.data[key])){
            delete this.data[key];
        }
    }
});