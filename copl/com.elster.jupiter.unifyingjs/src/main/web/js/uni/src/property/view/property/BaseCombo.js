/**
 * @class Uni.property.view.property.BaseCombo
 * @abstract
 *
 * This is base class for property which can be a combo (weird, better have separate propery for combo).
 * Do not use this class directly!
 *
 * @see: Uni.property.view.property.Text for example of custom property implementation
 */
Ext.define('Uni.property.view.property.BaseCombo', {
    extend: 'Uni.property.view.property.Base',

    requires: ['Uni.property.store.PossibleValues'],

    /**
     * @final
     * @returns {Object}
     */
    getEditCmp: function () {
        return this.isCombo() ? this.getComboCmp() : this.getNormalCmp();
    },

    /**
     * Return is component a combobox or not
     *
     * @returns {boolean}
     */
    isCombo: function () {
        return this.getProperty().getSelectionMode() === 'COMBOBOX';
    },

    /**
     * @abstract
     *
     * You must implement this method on inheritance
     */
    getNormalCmp: function() {
        throw 'getNormalCmp is not implemented';
    },

    getComboCmp: function () {
        var me = this,
            store = Ext.getStore('Uni.property.store.PossibleValues'),
            possibleValues = me.getProperty().getPossibleValues().sort(),
            propertyValue = undefined;
        store.loadData(possibleValues);
        Ext.each(possibleValues, function(possibleValue) {
            if(possibleValue.id === me.getProperty().get('value')){
                propertyValue = possibleValue.name;
            }
        });
        return {
            xtype: 'combobox',
            itemId: me.key + 'combobox',
            name: this.getName(),
            store: store,
            queryMode: 'local',
            displayField: 'name',
            valueField: 'id',
            width: me.width,
            value: propertyValue,
            forceSelection: me.getProperty().getExhaustive(),
            readOnly: me.isReadOnly,
            editable: !me.getProperty().getExhaustive(),
            allowBlank: !me.getProperty().data.required,
            blankText: me.blankText
        }
    },

    setValue: function (value) {
        if (this.isEdit) {
            this.isCombo()
                ? this.getComboField().setValue(value)
                : this.callParent(arguments);
        } else {
            this.callParent(arguments);
        }
    },

    getComboField: function () {
        return this.down('combobox');
    },

    markInvalid: function (error) {
        this.down('combobox').markInvalid(error);
    },

    clearInvalid: function (error) {
        this.down('combobox') && this.down('combobox').clearInvalid();
    },

    initListeners: function () {
        var me = this;
        this.callParent(arguments);
        var field = me.getComboField();

        if (field) {
            field.on('change', function () {
                if(this.getValue() === me.getProperty().get('default')) {
                    me.getProperty().set('isInheritedOrDefaultValue', true);
                } else {
                    me.getProperty().set('isInheritedOrDefaultValue', false);
                }
                me.updateResetButton();
            });
        }
    },

    doEnable: function(enable) {
        if (this.getField()) {
            if (this.isCombo()) {
                if (enable) {
                    this.getComboField().enable();
                } else {
                    this.getComboField().disable();
                }
            } else {
                this.callParent(arguments);
            }
        }
    }

});