/**
 * @class Uni.property.view.property.Base
 * @abstract
 *
 * This is base class for property.
 * Do not use this class directly!
 *
 * Use this class for implementation of custom properties.
 * The childs of this class must implement method:
 * getEditCmp() and getField()
 *
 * @see: Uni.property.view.property.Time for example of custom property implementation
 */
Ext.define('Uni.property.view.property.Base', {
    extend: 'Ext.form.FieldContainer',

    requires: [
        'Uni.property.view.DefaultButton'
    ],

    width: 256,
    translationKey: 'UNI',
    resetButtonHidden: false,

    layout: 'hbox',
    fieldLabel: '',
    required: false,

    items: [
        {
            xtype: 'uni-default-button'
        }
    ],

    isEdit: true,
    isReadOnly: false,
    inputType: 'text',
    property: null,
    key: null,
    passwordAsTextComponent: false,
    emptyText: '',
    userHasViewPrivilege: true,
    userHasEditPrivilege: true,

    /**
     * @param {string|null} key
     * @returns {string}
     */
    getName: function (key) {
        key = key ? key : this.key;
        return 'properties.' + key;
    },

    /**
     * @private
     * @param {string} key
     */
    setKey: function (key) {
        this.key = key;

        var label = Uni.I18n.translate(key, this.translationKey, key);
        this.setFieldLabel(label);
    },

    /**
     * Returns Reset button
     * @returns {Uni.property.view.DefaultButton}
     */
    getResetButton: function () {
        return this.down('uni-default-button');
    },

    /**
     * Performs property initialisation
     *
     * @private
     * @param {Uni.property.model.Property} property
     */
    initProperty: function (property) {
        var me = this;
        me.property = property;

        if (property) {
            me.key = property.get('key');
            me.itemId = me.key;

            if (me.isEdit) {
                me.required = property.get('required');
                if (me.required) {
                    me.allowBlank = false
                }
            }
        }
    },

    /**
     * Sets the property and update component values
     *
     * @param {Uni.property.model.Property} property
     */
    setProperty: function (property) {
        this.property = property;

        if (property) {
            this.setKey(property.get('key'));
            this.setValue(this.getProperty().get('value'));
            this.updateResetButton();
        }
    },

    /**
     * Updates the reset button state and tooltip
     */
    updateResetButton: function () {
        var resetButtonHidden = this.resetButtonHidden;
        var button = this.getResetButton();

        if (this.isEdit) {
            button.setVisible(!resetButtonHidden);
            if(!this.getProperty().get('isInheritedOrDefaultValue')){
                if (!this.getProperty().get('default')) {
                    button.setTooltip(Uni.I18n.translate('general.clear', 'UNI', 'Clear'));
                } else {
                    button.setTooltip(
                            Uni.I18n.translate('general.restoreDefaultValue', this.translationKey, 'Restore to default value')
                            + ' &quot; ' + this.getProperty().get('default') + '&quot;'
                    );
                }

                button.setDisabled(false);
            } else {
                button.setTooltip(null);
                button.setDisabled(true);
            }
        } else {
            button.setVisible(false);
        }
        
        this.fireEvent('checkRestoreAll', this);
    },

    /**
     *
     * shows a popup if entered value equals inheritedValue, this lets the user choose between deleting the property or
     * setting the new (same value) on the property
     */
    showPopupEnteredValueEqualsInheritedValue: function (field, property) {
        var me = this;
        Ext.create('Uni.view.window.Confirmation', {
            confirmText: Uni.I18n.translate('general.yes', 'UNI', 'Yes'),
            cancelText: Uni.I18n.translate('general.no', 'UNI', 'No')
        }).show({
            msg: Ext.String.format(Uni.I18n.translate('property.valueSameAsInherited', 'UNI', 'The value of \'{0}\' is the same as the default value.  Do you want to link the value to the default value?'), property.get('key')),
            title: Ext.String.format(Uni.I18n.translate('property.valueSameAs', 'MDC', 'Set \'{0}\' to its default value?'), property.get('key')),
            config: {
                property: me,
                field: field
            },
            fn: me.setPropertyValue
        });
    },

    setPropertyValue: function (btn, text, opt) {
        // var me = this;
        if (btn === 'confirm') {
            var property = opt.config.property;
            //var field = opt.config.field;
            property.restoreDefault();
        }
    },


    /**
     * returns bounded property
     * @returns {Uni.property.model.Property}
     */
    getProperty: function () {
        return this.property;
    },

    /**
     * @abstract
     *
     * You must implement this method on inheritance
     *
     * Example:
     *
     * getEditCmp: function () {
     *   var me = this;
     *   return {
     *       xtype: 'textfield',
     *       name: this.getName(),
     *       itemId: me.key + 'textfield',
     *       width: me.width,
     *       msgTarget: 'under'
     *   }
     * },
     */
    getEditCmp: function () {
        throw 'getDisplayCmp is not implemented';
    },

    /**
     * returns basic display field configuration
     * override this method if you want custom look of display field of custom property.
     *
     * @returns {Object}
     */
    getDisplayCmp: function () {
        return {
            xtype: 'displayfield',
            name: this.getName(),
            itemId: this.key + 'displayfield',
            cls: 'uni-property-displayfield'
        }
    },

    /**
     * Sets value to the view component
     * Override this method if you have custom logic of value transformation
     * @see Uni.property.view.property.Time for example
     *
     * @param value
     */
    setValue: function (value) {
        if (this.isEdit) {
            if (this.getProperty().get('hasValue') && !this.userHasViewPrivilege && this.userHasEditPrivilege) {
                this.getField().emptyText = Uni.I18n.translate('Uni.value.provided', 'UNI', 'Value provided - no rights to see the value.');
            } else {
                this.getField().emptyText = '';
            }
            this.getField().setValue(value);
        } else {
            if (this.getProperty().get('hasValue')) {
                this.getDisplayField().setValue('********');
            } else {
                this.getDisplayField().setValue(value);
            }
        }
    },

    getValue: function () {
        return this.getField().getValue()
    },

    /**
     * Return edit field
     * Implement this method on inheritance
     *
     * @returns {Uni.property.view.property.Base}
     */
    getField: function () {
        return null;
    },

    /**
     * Marking field as invalid and undo this
     * Implement this methods on inheritance
     *
     */

    markInvalid: function () {
        return null;
    },

    clearInvalid: function (error) {
        return null;
    },

    /**
     * Returns display field
     * @returns {*}
     */
    getDisplayField: function () {
        return this.down('displayfield');
    },

    /**
     * performs component initialisation
     */
    initComponent: function () {
        var me = this;
        var cfg = Ext.apply({items: []}, me.config);
        Ext.apply(cfg.items, me.items);

        me.initProperty(me.property);

        var cmp = me.isEdit
                ? me.getEditCmp()
                : me.getDisplayCmp()
            ;

        // apply config object or array of config objects
        if (Ext.isArray(cmp)) {
            var arguments = [0, 0];
            arguments.push.apply(arguments, cmp);
            cfg.items.splice.apply(cfg.items, arguments);
        } else if (Ext.isObject(cmp)) {
            cfg.items.splice(0, 0, cmp);
        }

        Ext.apply(me, cfg);
        me.callParent(arguments);

        // after init
        me.on('afterrender', function () {
            me.setProperty(me.property);
            me.initListeners();
        });
    },

    initListeners: function () {
        var me = this;
        var field = me.getField();

        if (field) {
            field.on('change', function () {
                me.getProperty().set('isInheritedOrDefaultValue', false);
                me.updateResetButton();
                if (field.getValue() === null || field.getValue() === '') {
                    me.getProperty().set('hasValue', false);
                    me.getProperty().set('propertyHasValue', false);
                }
                me.customHandlerLogic();
            });
            field.on('blur', function () {
                if (!field.hasNotValueSameAsDefaultMessage && field.getValue() !== '' && !me.getProperty().get('isInheritedOrDefaultValue') && field.getValue() === me.getProperty().get('default')) {
                    me.showPopupEnteredValueEqualsInheritedValue(field, me.getProperty());
                }
                if (field.getValue() === ''  && field.getValue() === me.getProperty().get('default')) {
                    me.getProperty().set('isInheritedOrDefaultValue', true);
                    me.updateResetButton();
                }
                me.customHandlerLogic();
            })
        }
        this.getResetButton().setHandler(this.restoreDefault, this);
    },

    customHandlerLogic: function(){
        //implement in propertycomponents that need custom logic on change;
    },

    /**
     * Restores default field value
     */
    restoreDefault: function () {
        var property = this.getProperty();
        var restoreValue = property.get('default');
        property.set('hasValue', false);
        property.set('propertyHasValue', false);
        this.setValue(restoreValue);
        property.set('isInheritedOrDefaultValue', true);
        this.updateResetButton();
    },

    /**
     * Sets inherited value as default
     */
    useInheritedValue: function () {
        this.getProperty().initInheritedValues();
        this.updateResetButton();
    },

    /**
     * show value
     */
    showValue: function () {
        if (this.isEdit) {
            this.getField().getEl().down('input').dom.type = 'text';
        } else {
            this.getDisplayField().setValue(this.getProperty().get('value'));
        }
    },

    /**
     * hide value
     */
    hideValue: function () {
        if (this.isEdit) {
            this.getField().getEl().down('input').dom.type = 'password';
        } else {
            if (this.getProperty().get('hasValue')) {
                this.getDisplayField().setValue('********');
            } else {
                this.getDisplayField().setValue('');
            }
        }
    }

});