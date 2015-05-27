Ext.define('Uni.property.view.property.Date', {
    extend: 'Uni.property.view.property.Base',

    format: Uni.DateTime.dateShortDefault,
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
            allowBlank: me.allowBlank,
            listeners: {
                change: {
                    fn: me.checkValidDate,
                    scope: me
                }
            }
        };
    },

    checkValidDate: function () {
        var me = this,
            date = me.getField().getValue();

        if (!Ext.isDate(date)) {
            me.getField().setValue(null);
        }
    },

    getField: function () {
        return this.down('datefield');
    },

    markInvalid: function (error) {
        this.down('datefield').markInvalid(error);
    },

    clearInvalid: function (error) {
        this.down('datefield').clearInvalid();
    },

    setValue: function (value) {
        if (value !== null && value !== '') {
            value = new Date(value);

            if (!this.isEdit) {
                value = Uni.DateTime.formatDateShort(value);
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