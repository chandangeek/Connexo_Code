Ext.define('Mdc.view.setup.deviceloadprofiles.SideFilterDuration', {
    extend: 'Ext.form.field.ComboBox',
    alias: 'widget.side-filter-duration',
    editable: false,
    multiSelect: true,
    queryMode: 'local',
    triggerAction: 'all',

    initComponent: function () {
        var me = this;
        me.listConfig = {
            getInnerTpl: function () {
                return '<div class="x-combo-list-item"><img src="' + Ext.BLANK_IMAGE_URL + '" class="x-form-checkbox" /> {' + me.displayField + '}</div>';
            }
        };

        me.callParent(arguments);
        me.store.on('load', function () {
            me.select(me.getValue());
            me.fireEvent('updateTopFilterPanelTagButtons', me);
        });
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
