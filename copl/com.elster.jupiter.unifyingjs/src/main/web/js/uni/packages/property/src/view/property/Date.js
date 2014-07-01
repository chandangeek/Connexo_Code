Ext.define('Uni.property.view.property.Date', {
    extend: 'Uni.property.view.property.Base',

    format: 'd/m/Y',
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
            required: me.required
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
    }
});