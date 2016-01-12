Ext.define('Uni.property.view.property.Time', {
    extend: 'Uni.property.view.property.Base',

    timeFormat: 'H:i:s',

    getEditCmp: function () {
        var me = this;

        return {
            xtype: 'timefield',
            name: this.getName(),
            itemId: me.key + 'timefield',
            format: me.timeFormat,
            width: me.width,
            required: me.required,
            readOnly: me.isReadOnly,
            inputType: me.inputType,
            allowBlank: me.allowBlank
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
    },

    getValue: function () {
        var value = this.getField().getValue();
        if (value != null && value != '') {
            var newDate = new Date(1970, 0, 1, value.getHours(), value.getMinutes(), value.getSeconds(), 0);
            return newDate.getTime() / 1000;
        } else {
            return value;
        }
    }
});