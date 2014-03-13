Ext.define('Isu.component.filter.model.Filter', {
    extend: 'Ext.data.Model',

    getPlainData: function() {
        return this.getData(true);
    }
});
