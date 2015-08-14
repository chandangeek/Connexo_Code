Ext.define('Uni.property.view.property.Multiselect', {
    extend: 'Uni.property.view.property.Base',
    requires: [
        'Ext.ux.form.MultiSelect'
    ],

    getEditCmp: function () {
        var me = this;

        return [
            {
                items: [
                    {
                        xtype: 'multiselect',
                        itemId: me.key + 'multiselect',
                        name: me.getName(),
                        allowBlank: me.allowBlank,
                        store: me.getProperty().getPredefinedPropertyValues().possibleValues(),
                        displayField: 'name',
                        valueField: 'id',
                        width: me.width,
                        height: 194,
                        readOnly: me.isReadOnly,
                        inputType: me.inputType,
                        msgTarget: 'multiselect-invalid-id-' + me.id,
                        validateOnChange: false,
                            listeners: {
                            change: function (field, newValue) {
                                var count = newValue.length;

                                if (me.itemId == 'intervalFlags') {
                                    field.nextSibling('#multiselectSelectedItemsInfo').update(Uni.I18n.translatePlural('multiselect.intervalflags.selected', count, 'UNI', '{0} interval flags selected'));
                                } else {
                                    field.nextSibling('#multiselectSelectedItemsInfo').update(Uni.I18n.translatePlural('multiselect.selected', count, 'UNI', '{0} items selected'));

                                }
                                                            },
                            fieldvaliditychange: function (field, isValid) {
                                field.nextSibling('#multiselectError').setVisible(!isValid);
                            }
                        }
                    },
                    {
                        xtype: 'component',
                        itemId: 'multiselectSelectedItemsInfo',
                        html: (me.itemId == 'intervalFlags')? Ext.String.format(Ext.String.format(Uni.I18n.translatePlural('multiselect.intervalflags.selected', 0, 'UNI', '{0} interval flags selected'), 0))
                            :Ext.String.format(Uni.I18n.translatePlural('multiselect.selected', 0, 'UNI', '{0} items selected'), 0)
                    },
                    {
                        xtype: 'component',
                        itemId: 'multiselectError',
                        cls: 'x-form-invalid-under',
                        hidden: true,
                        height: 36,
                        html: '<div id="multiselect-invalid-id-' + Ext.String.htmlEncode(me.id) + '"></div>'
                    }
                ]
            }
        ];
    },

    getDisplayCmp: function () {
        var me = this,
            store = me.getProperty().getPredefinedPropertyValues().possibleValues();

        return {
            xtype: 'displayfield',
            name: me.getName(),
            itemId: me.key + 'displayfield',
            renderer: function (data) {
                var result = '';

                Ext.isArray(data) && Ext.Array.each(data, function (item) {
                    var flag = store.getById(item);

                    flag && (result += Ext.String.htmlEncode(flag.get('name')) + '<br>');
                });

                return result;
            },
            cls: 'uni-property-displayfield'
        }
    },

    getField: function () {
        return this.down('multiselect');
    },

    setValue: function (value) {
        var field = this.getField();

        if (this.isEdit) {
            Ext.isArray(value) ? field.setValue(value) : field.reset();
        } else {
            this.getDisplayField().setValue(Ext.isArray(value) ? value : []);
        }
    }
});