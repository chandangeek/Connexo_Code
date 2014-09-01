Ext.define('Dsh.view.widget.common.SideFilterCombo', {
    extend: 'Ext.form.field.ComboBox',
    alias: 'widget.side-filter-combo',
    displayField: 'localizedValue',
    valueField: 'name',
    editable: false,
    multiSelect: true,
    queryMode: 'local',
    triggerAction: 'all',
    listConfig: {
        getInnerTpl: function () {
            return '<div class="x-combo-list-item"><img src="' + Ext.BLANK_IMAGE_URL + '" class="x-form-checkbox" /> {localizedValue} </div>';
        }
    },
    initComponent: function () {
        var me = this;
        this.callParent(arguments);
        this.store = Ext.create('Ext.data.Store', {
            autoLoad: true,
            fields: [
                { name: 'name', type: 'string' },
                { name: 'localizedValue', type: 'string' }
            ],
            proxy: {
                type: 'ajax',
                url: me.url,
                reader: {
                    type: 'json',
                    root: 'data'
                }
            }
        });
    }
});