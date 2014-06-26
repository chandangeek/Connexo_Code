Ext.define('Uni.property.view.property.DateTime', {
    extend: 'Uni.property.view.property.Date',

    timeFormat: 'H:i:s',

    getEditCmp: function () {
        var me = this;
        var result = this.callParent(arguments);

        result.splice(0, 0, {
            xtype: 'timefield',
            name: 'properties.' + me.key,
            itemId: me.key + 'timefield',
            format: me.timeFormat,
            width: me.width,
            required: me.required
        });

        return result;
    },

    getTimeField: function () {
        return this.down('timefield');
    },

    setValue: function (value) {
        var dateValue = null;
        var timeValue = null;

        if (value !== null && value !== '') {
            var date = new Date(value);
            dateValue = new Date(date.getFullYear(), date.getMonth(), date.getDate(), 0, 0, 0, 0);
            timeValue = new Date(1970, 0, 1, date.getHours(), date.getMinutes(), date.getSeconds(), 0);
        }

        if (!this.isEdit) {
            this.callParent([date.toLocaleString()]);
        } else {
            this.callParent([dateValue]);
            this.getTimeField().setValue(timeValue);
        }
    }
});