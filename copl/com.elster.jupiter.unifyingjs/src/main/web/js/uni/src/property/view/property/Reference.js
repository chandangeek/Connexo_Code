Ext.define('Uni.property.view.property.Reference', {
    extend: 'Uni.property.view.property.BaseCombo',

    referencesStore: Ext.create('Ext.data.Store', {
        fields: [
            {name: 'key', type: 'number'},
            {name: 'value', type: 'string'}
        ]
    }),

    getEditCmp: function () {
        var me = this;

        // clear store
        me.referencesStore.loadData([], false);
        for (var i = 0; i < me.getProperty().getPossibleValues().length; i++) {
            me.referencesStore.add({key: me.getProperty().getPossibleValues()[i].id, value: me.getProperty().getPossibleValues()[i].name})
        }

        return {
            xtype: 'combobox',
            itemId: me.key + 'combobox',
            name: this.getName(),
            store: me.referencesStore,
            queryMode: 'local',
            displayField: 'value',
            valueField: 'key',
            width: me.width,
            forceSelection: me.getProperty().getExhaustive(),
            readOnly: me.isReadOnly
        }
    },

    getDisplayCmp: function () {
        var me = this

        return {
            xtype: 'displayfield',
            name: me.getName(),
            itemId: me.key + 'displayfield'
        }
    },

    getValue: function () {
        debugger;
        return this.getField().getValue();
    },

    getField: function () {
        return this.down('combobox');
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
            this.getField().setValue(value.id);
        } else {
            if (this.getProperty().get('hasValue')) {
                this.getDisplayField().setValue('********');
            } else {
                this.getDisplayField().setValue(value.name);
            }
        }
    }

});