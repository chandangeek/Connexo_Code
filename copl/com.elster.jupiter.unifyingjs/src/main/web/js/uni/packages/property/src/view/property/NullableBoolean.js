Ext.define('Uni.property.view.property.NullableBoolean', {
    extend: 'Uni.property.view.property.Base',

    getEditCmp: function () {
        var me = this;

        return {
            xtype: 'radiogroup',
            itemId: me.key + 'radiogroup',
            name: this.getName(),
            allowBlank: false,
            vertical: true,
            columns: 1,
            items: [
                {
                    boxLabel: Uni.I18n.translate('true', me.translationKey, 'True'),
                    name: 'rb',
                    itemId: 'rb_1_' + me.key,
                    inputValue: true
                },
                {
                    boxLabel: Uni.I18n.translate('false', me.translationKey, 'False'),
                    name: 'rb',
                    itemId: 'rb_2_' + me.key,
                    inputValue: false
                },
                {
                    boxLabel: Uni.I18n.translate('none', me.translationKey, 'None'),
                    name: 'rb',
                    itemId: 'rb_3_' + me.key,
                    inputValue: null
                }
            ]
        };
    },

    getField: function () {
        return this.down('radiogroup');
    },

    setValue: function (value) {
        var result = {rb: null};
        if (Ext.isBoolean(value)) {
            result.rb = value;
        }

        if (!this.isEdit) {
            if (value === true) {
                result = Uni.I18n.translate('yes', this.translationKey, 'Yes');
            } else if (value === false) {
                result = Uni.I18n.translate('no', this.translationKey, 'No');
            } else {
                result = Uni.I18n.translate('na', this.translationKey, 'N/A');
            }
        }

        this.callParent([result]);
    }
});