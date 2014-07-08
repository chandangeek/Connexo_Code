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

    width: 320,
    resetButtonHidden: false,
    translationKey: 'UNI',

    layout: 'hbox',
    fieldLabel: '',
    required: false,

    items: [
        {
            xtype: 'defaultButton'
        }
    ],

    isEdit: true,
    property: null,
    key: null,

    /**
     * @param {string|null} key
     * @returns {string}
     */
    getName: function(key) {
        key = key ? key : this.key;
        return 'properties.' + key;
    },

    /**
     * @private
     * @param {string} key
     */
    setKey: function(key) {
        this.key = key;

        var label = Uni.I18n.translate(key, this.translationKey, key);
        this.setFieldLabel(label);
    },

    /**
     * Returns Reset button
     * @returns {Uni.property.view.DefaultButton}
     */
    getResetButton: function () {
        return this.down('defaultButton');
    },

    /**
     * Performs property initialisation
     *
     * @private
     * @param {Uni.property.model.Property} property
     */
    initProperty: function (property) {
        this.property = property;

        if (property) {
            this.key = property.get('key');
            this.itemId = this.key;

            if (this.isEdit) {
                this.required = property.get('required');
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

        if (!resetButtonHidden) {
            button.setTooltip(
                    Uni.I18n.translate('general.restoreDefaultValue', 'MDC', 'Restore to default value')
                    + ' &quot; ' + this.getProperty().get('default') + '&quot;'
            );

            button.setVisible(!this.getProperty().get('isInheritedOrDefaultValue'));
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
            itemId: this.key + 'displayfield'
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
        this.isEdit
            ? this.getField().setValue(value)
            : this.getDisplayField().setValue(value);
    },

    getValue: function (value) {
        return value;
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
        me.setProperty(me.property);
        me.initListeners();
    },

    initListeners: function () {
        var me = this;
        var field = me.getField();

        if (field) {
            field.on('change', function () {
                me.getProperty().set('isInheritedOrDefaultValue', false);
                me.updateResetButton();
            });
        }

        this.getResetButton().setHandler(this.restoreDefault, this);
    },

    /**
     * Restores default field value
     */
    restoreDefault: function () {
        var property = this.getProperty();
        var restoreValue = property.get('default');
        this.setValue(restoreValue);
        property.set('isInheritedOrDefaultValue', true);

        this.updateResetButton();
    },

    /**
     * Sets inherited value as default
     */
    useInheritedValue: function() {
        this.getProperty().initInheritedValues();
        this.updateResetButton();
    }
});