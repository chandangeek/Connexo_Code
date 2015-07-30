Ext.define('Uni.property.view.property.RelativePeriod', {
    extend: 'Uni.property.view.property.Base',
    requires: [
        'Uni.property.store.RelativePeriods'
    ],
    getEditCmp: function () {
        var me = this;

        return [
            {
                xtype: 'container',
                layout: 'vbox',
                items: [
                    {
                        xtype: 'radiogroup',
                        name: this.getName(),
                        itemId: 'relativeRadioGroup',
                        width: me.width,
                        columns: 1,
                        vertical: true,
                        msgTarget: 'under',
                        readOnly: me.isReadOnly,
                        fieldLabel: me.boxLabel ? me.boxLabel : '',
                        items: [
                            {boxLabel: Uni.I18n.translate('general.all', 'UNI', 'All'), name: 'relative', inputValue: '1'},
                            {boxLabel: Uni.I18n.translate('general.period', 'UNI', 'Period'), name: 'relative', inputValue: '2'}
                        ]
                    },
                    {
                        xtype: 'combobox',
                        fieldLabel: '',
                        width: me.width,
                        disabled: false,
                        displayField: 'name',
                        valueField: 'id',
                        store: 'Uni.property.store.RelativePeriods'
                    }
                ]
            }
        ];
    },

    initListeners: function () {
        var me = this;
        if (this.down('combobox')) {
            this.down('combobox').on('change', function () {
                me.getProperty().set('isInheritedOrDefaultValue', false);
                me.updateResetButton();
            });
        }
        this.callParent(arguments);
    },

    customHandlerLogic: function(){
        var field = this.getField();
        if(field.getValue().relative==='1'){
            this.down('combobox').setDisabled(true);
            this.down('combobox').setValue(null);
        } else {
            this.down('combobox').setDisabled(false);
        }
    },

    getField: function () {
        return this.down('#relativeRadioGroup');
    },

    doEnable: function(enable) {
        if (this.getField()) {
            if (enable) {
                this.getField().enable();
                this.down('combobox').enable();
            } else {
                this.getField().disable();
                this.down('combobox').disable();
            }
        }
    },

    setValue: function (value) {
        if (!this.isEdit) {
            this.getDisplayField().setValue(this.getValueAsDisplayString(value));
        } else {
            if(value.id !== 0){
                this.down('#relativeRadioGroup').setValue({relative:2});
                this.down('combobox').setValue(value.id);
                this.down('combobox').setDisabled(false);
            } else {
                this.down('#relativeRadioGroup').setValue({relative:1});
                this.down('combobox').setDisabled(true);
            }
        }
    },

    getValue: function () {
        var me = this;
        if(me.down('#relativeRadioGroup').getValue().relative==='1'){
            return {
                id: 0
            }
        } else {
            return {
                id: me.down('combobox').getValue()
            }
        }

    },

    getDisplayCmp: function () {
        var me = this;

        return {
            xtype: 'displayfield',
            name: this.getName(),
            itemId: me.key + 'displayfield',
            width: me.width,
            msgTarget: 'under',
            cls: 'uni-property-displayfield'
        }
    },


    getDisplayField: function () {
        return this.down('displayfield');
    },

    initComponent: function(){
        var periods = Ext.getStore('Uni.property.store.RelativePeriods');
        periods.load();
        this.callParent(arguments);
    },

    getValueAsDisplayString: function (value) {
        if (Ext.isObject(value)) {
            if(value.id !== 0){
                return value.name;
            } else {
                return Uni.I18n.translate('general.all', 'UNI', 'All');
            }
        }
        this.callParent(arguments);
    }

});



