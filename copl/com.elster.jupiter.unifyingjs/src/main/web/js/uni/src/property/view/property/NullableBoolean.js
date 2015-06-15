Ext.define('Uni.property.view.property.NullableBoolean', {
    extend: 'Uni.property.view.property.Base',

    getEditCmp: function () {
        var me = this;

        return {
            xtype: 'radiogroup',
            itemId: me.key + 'radiogroup',
            name: this.getName(),
            allowBlank: me.allowBlank,
            vertical: true,
            columns: 1,
            readOnly: me.isReadOnly,
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
                    boxLabel: Uni.I18n.translate('general.none', me.translationKey, 'None'),
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
            result = this.getValueAsDisplayString(value);
        }

        this.callParent([result]);
    },

    getValueAsDisplayString: function (value) {
        if (Ext.isBoolean(value)) {
            if (value === true) {
                return Uni.I18n.translate('general.yes', this.translationKey, 'Yes');
            } else if (value === false) {
                return Uni.I18n.translate('general.no', this.translationKey, 'No');
            } else {
                return Uni.I18n.translate('general.none', this.translationKey, 'None');
            }
        } else {
            return callParent(arguments);
        }
    }

});