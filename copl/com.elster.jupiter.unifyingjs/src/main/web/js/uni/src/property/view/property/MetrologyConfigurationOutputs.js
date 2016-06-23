Ext.define('Uni.property.view.property.MetrologyConfigurationOutputs', {
    extend: 'Uni.property.view.property.Base',

    msgTarget: 'under',
    getEditCmp: function () {
        var me = this,
            store = Ext.create('Ext.data.Store', {
                model: 'Uni.model.ReadingType',
                proxy: {
                    proxy: {
                        type: 'rest',
                        url: '/api/mtr/metrologyconfigurations/' + me.up('property-form').context.id + 'readingtypes',
                        reader: {
                            type: 'json',
                            root: 'readingTypes'
                        }
                    }
                }
            });

        store.load();

        return [
            {
                xtype: 'grid',
                itemId: 'metrology-configuration-outputs-grid',
                store: store,
                width: me.width,
                selModel: {
                    mode: 'MULTI',
                    checkOnly: true,
                    showHeaderCheckbox: false,
                    pruneRemoved: false,
                    updateHeaderState: Ext.emptyFn
                },
                columns: [
                    {
                        header: Uni.I18n.translate('general.name', 'UNI', 'Name'),
                        dataIndex: 'name',
                        flex: 1
                    },
                    {
                        header: Uni.I18n.translate('general.readingType', 'UNI', 'Reading type'),
                        dataIndex: 'readingType',
                        flex: 1
                    }
                ]
            }
        ];
    },

    getField: function () {
        return this.down('numberfield');
    },

    setValue: function (value) {
    },

    getValue: function () {
        var me = this;

        return _.map(me.down('grid').getSelectionModel().getSelection(), function (record) {
            return record.getId();
        });
    },

    markInvalid: function (error) {
        var me = this;

        me.toggleInvalid(error);
    },

    clearInvalid: function () {
        var me = this;

        me.toggleInvalid();
    },

    toggleInvalid: function (error) {
        var me = this,
            oldError = me.getActiveError();

        Ext.suspendLayouts();
        me.items.each(function (item) {
            if (item.isFormField) {
                if (error) {
                    item.addCls('x-form-invalid');
                } else {
                    item.removeCls('x-form-invalid');
                }
            }
        });
        if (error) {
            me.setActiveErrors(error);
        } else {
            me.unsetActiveError();
        }
        if (oldError !== me.getActiveError()) {
            me.doComponentLayout();
        }
        Ext.resumeLayouts(true);
    },

    getValueAsDisplayString: function (value) {
        var me = this;

        return '-';
    }
});