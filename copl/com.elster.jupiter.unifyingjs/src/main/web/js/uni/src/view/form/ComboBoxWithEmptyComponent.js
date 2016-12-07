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

    config: {
        noObjectsText: Uni.I18n.translate('general.noObjectsDefinedYet', 'UNI', 'No objects defined yet'),
        store: null,
        name: null,
        displayField: null,
        valueField: null,
        allowBlank: false,
        emptyText: Uni.I18n.translate('general.selectAnObject', 'UNI', 'Select an object...')
    },

    initComponent: function () {
        var me = this,
            storeObject,
            combo;
        me.initConfig(me.config);
        storeObject = Ext.data.StoreManager.lookup(me.getStore());
        storeObject.load({
            callback: function (record, operation, success) {
                me.removeAll();
                if (success && storeObject.count() > 0) {
                    combo = Ext.create('Ext.form.ComboBox', {
                        queryMode: 'local',
                        itemId: me.itemId,
                        store: storeObject,
                        allowBlank: me.allowBlank,
                        width: me.config.width - me.getLabelWidth(),
                        displayField: me.config.displayField,
                        valueField: me.config.valueField,
                        emptyText: me.config.emptyText
                    });
                    me.add(combo);
                } else {
                    me.add({
                        xtype: 'displayfield',
                        itemId: 'noObjectsFoundField',
                        fieldLabel: '',
                        value: me.getNoObjectsText(),
                        fieldStyle: {
                            color: '#EB5642'
                        },
                        validate: function () {
                            return me.config.allowBlank;
                        }

                    })
                }

            }
        });

        me.callParent(arguments)
    }
});