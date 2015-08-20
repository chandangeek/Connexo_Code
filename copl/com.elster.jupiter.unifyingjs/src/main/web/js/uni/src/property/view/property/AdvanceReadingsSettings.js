Ext.define('Uni.property.view.property.AdvanceReadingsSettings', {
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
                            {boxLabel: Uni.I18n.translate('advanceReadingProperty.None','UNI','None'), name: 'advanceRb', inputValue: '1'},
                            {boxLabel:  Uni.I18n.translate('advanceReadingProperty.bulkReading','UNI','Bulk Reading'), name: 'advanceRb', inputValue: '2'},
                            {boxLabel: Uni.I18n.translate('general.readingType','UNI','Reading type'), name: 'advanceRb', inputValue: '3'}
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
                        store: this.readingTypes,//'Uni.property.store.PropertyReadingTypes',
                        listConfig: {
                            cls: 'isu-combo-color-list',
                            emptyText: Uni.I18n.translate('general.readingtype.noreadingtypefound', 'UNI', 'No readingtype found')
                        },

                        queryMode: 'remote',
                        queryParam: 'like',
                        queryDelay: 100,
                        queryCaching: false,
                        minChars: 1,
                        editable:true,
                        typeAhead:true,
                        // anchor: '100%',
                        emptyText: Uni.I18n.translate('general.readingtype.selectreadingtype', 'UNI', 'Start typing to select a reading type...')
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
        if(field.getValue().advanceRb==='1' || field.getValue().advanceRb==='2'){
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
            this.getDisplayField().setValue(this.getValueAsDisplayString(value));
        } else {
            if(value.none){
                this.down('radiogroup').setValue({advanceRb:1});
                this.down('combobox').setDisabled(true);
            } else if (value.bulk) {
                this.down('radiogroup').setValue({advanceRb:2});
                this.down('combobox').setDisabled(true);
            } else {
                this.down('radiogroup').setValue({advanceRb:3});
                var readingTypeStore = me.down('#readingTypeCombo').getStore();
                readingTypeStore.load({
                    params: {like: value.readingType && value.readingType.aliasName},
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
        if(me.down('#readingRadioGroup').getValue().advanceRb==='1'){
            return {
                none: true,
                bulk: false
            }
        } else if(me.down('#readingRadioGroup').getValue().advanceRb==='2'){
            return {
                none: false,
                bulk: true
            }
        } else {
            var value = me.down('#readingTypeCombo').getValue(),
                record = me.down('#readingTypeCombo').findRecordByValue(value),
                readingType = record ? record.getData() : { mRID: value };

            return {
                none: false,
                bulk: false,
                readingType: readingType
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
    },

    //this method is not needed here. Please check listeners.
    doEnable: function() {
        return null;
    },

    markInvalid: function (error) {
        this.down('combobox').markInvalid(error);
    },

    clearInvalid: function (error) {
        this.down('combobox').clearInvalid();
    },

    getValueAsDisplayString: function (value) {
        if (Ext.isObject(value)) {
            if(value.none){
                return Uni.I18n.translate('general.none', this.translationKey, 'None');
            } else if (value.bulk){
                return Uni.I18n.translate('advanceReadingProperty.bulkReading', this.translationKey, 'Bulk Reading');
            } else {
                return value.readingType.aliasName;
            }
        }
        return this.callParent(arguments);
    }

});