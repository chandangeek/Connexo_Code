Ext.define('Uni.property.view.property.Date', {
    extend: 'Uni.property.view.property.Base',

    format: 'd M \'y',
    formats: [
        'd.m.Y',
        'd m Y'
    ],

    getEditCmp: function () {
        var me = this;

        return {
            xtype: 'datefield',
            name: this.getName(),
            itemId: me.key + 'datefield',
            format: me.format,
            altFormats: me.formats.join('|'),
            width: me.width,
            required: me.required,
            readOnly: me.isReadOnly,
            inputType: me.inputType,
            allowBlank: me.allowBlank
        };
    },

    getField: function () {
        return this.down('datefield');
    },

    setValue: function (value) {
        if (value !== null && value !== '') {
            value = new Date(value);

            if (!this.isEdit) {
                value = value.toLocaleDateString();
            }
        }
        this.callParent([value]);
    },

    getValue: function () {
        if (this.getField().getValue() != null) {
            return this.getField().getValue().getTime()
        } else {
            return null;
        }
    }
});