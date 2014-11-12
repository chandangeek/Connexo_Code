/**
 * @class Uni.view.form.CheckboxGroup
 * This is the checkboxgroup extension, which allows to auto-load checkboxes from bounded store/
 *
 * Example:
 *   {
 *      xtype: 'checkboxstore',
 *      fieldLabel: 'Select users',
 *      store: 'App.store.Users',
 *      columns: 1,
 *      vertical: true,
 *      name: 'users'
 *   }
 */
Ext.define('Uni.view.form.CheckboxGroup', {
    extend: 'Ext.form.CheckboxGroup',
    alias: 'widget.checkboxstore',

    mixins: {
        bindable: 'Ext.util.Bindable'
    },

    /**
     * This field will be used as boxLabel on checkbox
     */
    displayField: 'name',

    /**
     * This field will define the value of checkbox
     */
    valueField: 'id',

    /**
     * This field will define how the values will be returned by getModelData
     * if true the getModelData will return a list of ids that will be used by hydrator to fill the store by loading records.
     * otherwise the hydrator will fill the store using provided records.
     * Default is 'true' for backward compatibility.
     */
    hydratable: true,

    initComponent: function () {
        var me = this;
        me.bindStore(me.store || 'ext-empty-store', true);
        this.callParent(arguments);
    },

    /**
     * @private
     * Refreshes the content of the checkbox group
     */
    refresh: function () {
        var me = this;
        me.removeAll();
        me.store.each(function (record) {
            me.add({
                xtype: 'checkbox',
                boxLabel: record.get(me.displayField),
                inputValue: record.get(me.valueField),
                name: me.name,
                getModelData: function () {
                    return null;
                }
            });
        });
    },

    getModelData: function () {
        var me = this,
            groups = [],
            object = {};

        Ext.Array.each(me.query('checkbox'), function (checkbox) {
            if (checkbox.getValue()) {
                me.store.each(function (group) {
                    if (group.get(me.valueField) === checkbox.inputValue) {
                        if(me.hydratable){
                            groups.push(group.getId());
                        }
                        else{
                            groups.push(group);
                        }
                    }
                });
            }
        });

        object[me.name] = groups;

        return object;
    },

    setValue: function (data) {
        var values = {};
        values[this.name] = data;
        this.callParent([values]);
    },

    getStoreListeners: function () {
        return {
            load: this.refresh
        };
    }
});