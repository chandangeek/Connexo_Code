Ext.define('Uni.property.view.property.Time', {
    extend: 'Uni.property.view.property.Base',

    timeFormat: 'H:i:s',

    getEditCmp: function () {
        var me = this;

        return {
            xtype: 'timefield',
            name: 'properties.' + me.key,
            itemId: me.key + 'timefield',
            format: me.timeFormat,
            width: me.width,
            required: me.required
        };
    },

    getField: function () {
        return this.down('timefield');
    },

    setValue: function (value) {
        if (value !== null && value !== '') {
            value = new Date(value * 1000);

            if (!this.isEdit) {
                value = value.toLocaleTimeString();
            }
        }

        this.callParent([value]);
    }
});