Ext.define('Uni.property.view.property.DateTime', {
    extend: 'Uni.property.view.property.Date',

    timeFormat: 'H:i:s',

    getEditCmp: function () {
        var me = this,
            result = [];

        result[0] = this.callParent(arguments);
        result[1] = {
            xtype: 'timefield',
            name: me.getName() + '.time',
            margin: '0 0 0 16',
            itemId: me.key + 'timefield',
            format: me.timeFormat,
            width: me.width,
            required: me.required,
            readOnly: me.isReadOnly,
            inputType: me.inputType
        };

        return result;
    },

    getTimeField: function () {
        return this.down('timefield');
    },

    getDateField: function () {
        return this.down('datefield');
    },

    setValue: function (value) {
        var dateValue = null,
            timeValue = null;

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
    },

    getValue: function (value) {
        var timeValue = this.getTimeField().getValue(),
            dateValue = this.getDateField().getValue();

        if (timeValue !== null && timeValue !== '' && dateValue !== null && dateValue !== '') {
            var newDate = new Date(dateValue.getFullYear(), dateValue.getMonth(), dateValue.getDate(),
                timeValue.getHours(), timeValue.getMinutes(), timeValue.getSeconds(), 0);
            return newDate.getTime();
        }

        return null;
    }
});