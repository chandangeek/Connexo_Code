Ext.define('Uni.data.model.Filter', {
    extend: 'Ext.data.Model',

    /**
     * returns data in a format of filter:
     * [{property: key, value: item}]
     */
    getFilterData: function() {
        var me = this,
            data = this.getData(),
            filters = [];

        _.map(data, function (item, key) {
            if (item) {
                if (!_.isArray(item)) {item = [item]}
                filters.push({property: key, value: item});
            }
        });

        return filters;
    }
});