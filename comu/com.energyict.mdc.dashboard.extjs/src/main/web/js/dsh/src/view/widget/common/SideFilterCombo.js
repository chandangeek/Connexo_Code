Ext.define('Dsh.view.widget.common.SideFilterCombo', {
    extend: 'Ext.form.field.ComboBox',
    alias: 'widget.side-filter-combo',
    editable: false,
    multiSelect: true,
    queryMode: 'local',
    triggerAction: 'all',
    initComponent: function () {
        var me = this;
        me.on('afterrender', function () {
            me.store.load({
                callback: function () {
                    var form = me.up('form');
                        record = form.getRecord();
                    if (!_.isEmpty(record)) {
                        me.select(record.get(me.name));
                       form.fireEvent('fieldfirstload', me)
                    }
                }
            });
        });

        me.callParent(arguments);
        me.listConfig = {
            getInnerTpl: function () {
                return '<div class="x-combo-list-item"><img src="' + Ext.BLANK_IMAGE_URL + '" class="x-form-checkbox" /> {' + me.displayField + '}</div>';
            }
        };
    },
    getValue: function () {
        var me = this;
        me.callParent(arguments);
        if (_.isArray(me.value)) {
            me.value = _.compact(me.value)
        }
        return me.value
    }
});