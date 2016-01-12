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
                            {boxLabel: 'All', name: 'relative', inputValue: '1'},
                            {boxLabel: 'Period', name: 'relative', inputValue: '2'}
                        ]
                    },
                    {
                        xtype: 'combobox',
                        fieldLabel: '',
                        width: me.width,
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
        } else {
            this.down('combobox').setDisabled(false);
        }
    },

    getField: function () {
        return this.down('#relativeRadioGroup');
    },

    setValue: function (value) {
        if (!this.isEdit) {
            if(value.id !== 0){
                this.getDisplayField().setValue(value.name);
            } else {
                this.getDisplayField().setValue('All');
            }
        } else {
            if(value.id !== 0){
                this.down('#relativeRadioGroup').setValue({relative:2});
                this.down('combobox').setValue(value.id);
            } else {
                this.down('#relativeRadioGroup').setValue({relative:1});
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
            msgTarget: 'under'
        }
    },


    getDisplayField: function () {
        return this.down('displayfield');
    },

    initComponent: function(){
        var periods = Ext.getStore('Uni.property.store.RelativePeriods');
        periods.load();
        this.callParent(arguments);
    }
});



