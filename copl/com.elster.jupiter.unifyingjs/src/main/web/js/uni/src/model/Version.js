Ext.define('Uni.model.Version', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'version',
            type: 'int'
        }
    ],
    getRecordData: function () {
        return this.getProxy().getWriter().getRecordData(this);
    }
});