Ext.define('Uni.view.search.field.HasString', {
    extend: 'Uni.view.search.field.internal.Criteria',
    xtype: 'uni-search-criteria-has-string',
    text: '',
    minWidth: 225,
    value: 1,

    defaults: {
        margin: 0,
        padding: 5
    },

    setValue: function(value) {
        if (value) {
            this.down('radiofield[inputValue="' + value[0].get('criteria')[0] + '"]').setValue(true);
        } else {
            this.down('radiofield[inputValue="' + this.value + '"]').setValue(true);
        }
    },

    getValue: function () {
        var me = this,
            yesRadio = me.down('#uni-search-criteria-has-string-radio-yes'),
            textField = me.down('#uni-search-criteria-has-string-textfield');

        if (yesRadio.getValue()) {
            return [Ext.create('Uni.model.search.Value', {
                operator: Ext.isEmpty(textField.getValue()) ? 'ISDEFINED' : '==',
                criteria: textField.getValue()
            })];
        } else {
            return [Ext.create('Uni.model.search.Value', {
                operator: 'ISNOTDEFINED',
                criteria: ''
            })];
        }
    },

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'container',
                layout: {
                    type: 'hbox'
                },
                items: [
                    {
                        xtype: 'radiofield',
                        boxLabel: Uni.I18n.translate('general.yes', 'UNI', 'Yes'),
                        name: me.dataIndex,
                        inputValue: "1",
                        itemId: 'uni-search-criteria-has-string-radio-yes',
                        handler: me.onValueChange,
                        scope: me
                    },
                    {
                        xtype: 'textfield',
                        itemId: 'uni-search-criteria-has-string-textfield',
                        emptyText: Uni.I18n.translate('general.enterName.optional', 'UNI', 'Enter name (optional)'),
                        margins: '0 0 0 10',
                        listeners: {
                            change: {
                                fn: 'onArgumentChange',
                                scope: me
                            }
                        }
                    }
                ]
            },
            {
                xtype: 'menuseparator',
                padding: 0
            },
            {
                xtype: 'radiofield',
                boxLabel: Uni.I18n.translate('general.no', 'UNI', 'No'),
                name: me.dataIndex,
                inputValue: "0",
                itemId: 'uni-search-criteria-has-string-radio-no',
                handler: me.onValueChange,
                scope: me
            }
        ];

        me.callParent(arguments);
    },

    onValueChange: function(radioField, newValue) {
        var me = this,
            textField = me.down('#uni-search-criteria-has-string-textfield');

        textField.setDisabled(
            (radioField.itemId === 'uni-search-criteria-has-string-radio-no' && newValue)
            ||
            (radioField.itemId === 'uni-search-criteria-has-string-radio-yes' && !newValue)
        );

        me.fireEvent('change', me, me.getValue());
    },

    onArgumentChange: function() {
        var me = this;
        me.fireEvent('change', me, me.getValue());
    }

});