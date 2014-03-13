Ext.define('Isu.component.filter.model.Filter', {
    extend: 'Ext.data.Model',

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

    extractHasOne: function(record, association) {
        return record ? record.getId() : false;
    },

    extractHasMany: function(store, association) {
        var result = [];
        store.each(function(record){
            result.push(record.getId());
        });
        return result;
    }
});