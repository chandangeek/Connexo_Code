/**
 * @class Uni.view.form.ComboBoxWithEmptyComponent
 * This is a combobox which will show a text if the store of the combobox is empty
 *
 * Example:
 *   {
 *      xtype: 'comboboxwithemptycomponent',
 *      fieldLabel: 'Select users',
 *      store: 'App.store.Users',
 *      columns: 1,
 *      name: 'users'
 *   }
 */
Ext.define('Uni.view.form.ComboBoxWithEmptyComponent', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.comboboxwithemptycomponent',

    mixins: {
        bindable: 'Ext.util.Bindable'
    },

    config: {
        noObjectsText: Uni.I18n.translate('general.noObjectsDefinedYet', 'UNI', 'No objects defined yet'),
        store: null,
        name: null,
        displayField: null,
        valueField: null,
        emptyText: Uni.I18n.translate('general.selectAnObject', 'UNI', 'Select an object...'),
    },

    initComponent: function () {
        var me = this,
            storeObject,
            combo;
        me.initConfig(me.config);
        storeObject =  Ext.data.StoreManager.lookup(me.getStore());
        me.config.store = storeObject;
        storeObject.load({
            callback: function (record, operation, success) {
                me.removeAll();
                if (success && storeObject.count() > 0) {
                    combo = Ext.create('Ext.form.ComboBox');
                    Ext.apply(combo, me.config);
                    combo.setWidth(me.config.width - me.getLabelWidth());
                    me.add(combo);
                } else {
                    me.add({
                        xtype: 'displayfield',
                        fieldLabel: '',
                        value: me.getNoObjectsText(),
                        fieldStyle: {
                            color: '#EB5642'
                        }
                    })
                }

            }
        });

        me.callParent(arguments)
    }
});