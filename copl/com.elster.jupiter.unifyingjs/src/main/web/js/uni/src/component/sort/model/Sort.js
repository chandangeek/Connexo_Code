/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.component.sort.model.Sort
 *
 * Model of sorting params.
 * params should be set via model fields:
 *
 * @Example
 *
 *  Ext.define('App.model.Sort', {
 *      extend: 'Uni.component.sort.model.Sort',
 *      fields: [{
 *           name: 'dueDate',
 *           displayValue: 'Due date'
 *      }, {
 *          name: 'created',
 *          displayValue: 'Created time'
 *      }]
 *  });
 *
 *  var sort = new App.model.Sort();
 *
 *  sort.addSortParam('dueDate');
 *  sort.addSortParam('created', App.model.Sort.ASC);
 *
 *  Model allows you to get plain data
 *
 *  @Example
 *
 *  {
 *      'sort': ['dueDate', '-created']
 *  }
 *
 */
Ext.define('Uni.component.sort.model.Sort', {
    extend: 'Ext.data.Model',

    inheritableStatics: {
        /**
         * @property
         * @static
         */
        ASC : 'asc',

        /**
         * @property
         * @static
         * @private
         */
        DESC: 'desc'
    },

    defaultOrder: 'ASC',

    /**
     * returned object key property
     */
    key: 'sort',

    /**
     * Returns array of fields and association names
     *
     * @returns {String[]}
     */
    getFields: function() {
        return [this.key];
    },

    /**
     * Add sorting param to the model. If sort order is not present it uses default value.
     *
     * @param key sorting key
     * @param order sorting order
     */
    addSortParam: function(key, order) {
        order = order || this.statics()[this.defaultOrder];

        var field = this.fields.getByKey(key);
        if (field) {
            this.set(key, order);
        }
    },

    /**
     * Toddles sorting direction of the specified param
     *
     * @param key sorting key
     */
    toggleSortParam: function(key) {
        var field = this.fields.getByKey(key);

        if (field) {
            var order = this.get(key) == this.statics().ASC
                ? this.statics().DESC
                : this.statics().ASC
            ;

            this.set(key, order);
        }
    },

    /**
     * Removes param from sorting
     *
     * @param key sorting key
     */
    removeSortParam: function(key) {
        delete this.data[key];
    },

    /**
     * Returns plain object of sorting params
     *
     * @returns {Object}
     */
    getPlainData: function() {
        var data = this.getData(),
            map = {};

        map[this.statics().ASC] = '';
        map[this.statics().DESC] = '-';

        var params = [];
        _.each(data, function(item, key) {
            if (_.contains(_.keys(map), item)) {
                params.push(map[item] + key);
            }
        });

        var result = {};
        result[this.key] = params;

        return result;
    }
});