Ext.define('Isu.component.filter.model.Filter', {
    extend: 'Ext.data.Model',

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
                    data[association.name] = me.extractHasOne(me.get(association.name), association);
                    break;
                case 'hasMany':
                    data[association.name] = me.extractHasMany(me[association.name](), association);
                    break;
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
    }
});