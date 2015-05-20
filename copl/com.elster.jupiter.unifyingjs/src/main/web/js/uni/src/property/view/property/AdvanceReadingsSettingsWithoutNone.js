Ext.define('Uni.property.view.property.AdvanceReadingsSettingsWithoutNone', {
    extend: 'Uni.property.view.property.Base',
    requires: [
        'Uni.form.field.ReadingTypeCombo'
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
                        itemId: 'readingRadioGroup',
                        width: me.width,
                        columns: 1,
                        vertical: true,
                        msgTarget: 'under',
                        readOnly: me.isReadOnly,
                        fieldLabel: me.boxLabel ? me.boxLabel : '',
                        items: [
                            {boxLabel:  Uni.I18n.translate('advanceReadingProperty.bulkReading','UNI','Bulk Reading'), name: 'advanceRb', inputValue: '2'},
                            {boxLabel: Uni.I18n.translate('advanceReadingProperty.readingType','UNI','Reading type'), name: 'advanceRb', inputValue: '3'}
                        ]
                    },
                    {
                        itemId: 'readingTypeCombo',
                        xtype: 'reading-type-combo',
                        name: 'readingTypeCombo',
                        fieldLabel: '',
                        width: me.width,
                        displayField: 'fullAliasName',
                        valueField: 'mRID',
                        forceSelection: true,
                        store: this.readingTypes, //'Uni.property.store.PropertyReadingTypes',

                        listConfig: {
                            cls: 'isu-combo-color-list',
                            emptyText: Uni.I18n.translate('general.readingtype.noreadingtypefound', 'MDC', 'No readingtype found')
                        },

                        queryMode: 'remote',
                        queryParam: 'like',
                        queryDelay: 100,
                        queryCaching: false,
                        minChars: 1,
                        editable:true,
                        typeAhead:true,
                        // anchor: '100%',
                        emptyText: Uni.I18n.translate('general.readingtype.selectreadingtype', 'MDC', 'Start typing to select a reading type...')
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
        if(field.getValue().advanceRbNone==='2'){
            this.down('combobox').setDisabled(true);
        } else {
            this.down('combobox').setDisabled(false);
        }
    },

    getField: function () {
        return this.down('radiogroup');
    },

    setValue: function (value) {
        var me = this;
        if (!this.isEdit) {
            if (value.bulk){
                this.getDisplayField().setValue('Bulk');
            } else {
                this.getDisplayField().setValue(value.readingType.name);
            }
        } else {
            if (value.bulk) {
                this.down('radiogroup').setValue({advanceRbNone:2});
                this.down('combobox').setDisabled(true);
            } else {
                this.down('radiogroup').setValue({advanceRbNone:3});
                var readingTypeStore = me.down('#readingTypeCombo').getStore();
                readingTypeStore.load({
                    callback: function () {
                        var model = Ext.create('Mdc.model.ReadingType',value.readingType);
                        me.down('#readingTypeCombo').setValue(model);
                    }
                });
            }
        }
    },

    getValue: function () {
        var me = this;
        if(me.down('#readingRadioGroup').getValue().advanceRbNone==='2'){
            return {
                bulk: true
            }
        } else {
            return {
                bulk: false,
                readingType: {
                    mRID: me.down('#readingTypeCombo').getValue()
                }
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
        this.readingTypes = Ext.create('Uni.property.store.PropertyReadingTypes');
        this.callParent(arguments);
    }
});