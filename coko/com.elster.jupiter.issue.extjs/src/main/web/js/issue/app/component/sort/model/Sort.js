Ext.define('Isu.component.sort.model.Sort', {
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

    default: 'ASC',
    key: 'sort',

    /**
     * Returns array of fields and association names
     *
     * @returns {String[]}
     */
    getFields: function() {
        return [this.key];
    },

    addSortParam: function(key, order) {
        order = order || this.statics()[this.default];

        var field = this.fields.getByKey(key);
        if (field) {
            this.set(key, order);
        }
    },

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

    removeSortParam: function(key) {
        delete this.data[key];
    },

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