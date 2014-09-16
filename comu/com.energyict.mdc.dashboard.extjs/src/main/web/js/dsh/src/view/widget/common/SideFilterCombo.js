Ext.define('Dsh.view.widget.common.SideFilterCombo', {
    extend: 'Ext.form.field.ComboBox',
    alias: 'widget.side-filter-combo',
    editable: false,
    multiSelect: true,
    queryMode: 'local',
    triggerAction: 'all',
    initComponent: function () {
        var me = this;
        this.callParent(arguments);
        this.listConfig = {
            getInnerTpl: function () {
                return '<div class="x-combo-list-item"><img src="' + Ext.BLANK_IMAGE_URL + '" class="x-form-checkbox" /> {' + me.displayField + '}</div>';
            }
        };
        this.store = Ext.create('Ext.data.Store', {
            autoLoad: true,
            fields: [me.valueField, me.displayField],
            proxy: {
                type: 'ajax',
                url: me.url,
                reader: {
                    type: 'json',
                    root: me.root
                }
            },
            listeners: {
                load: function () {
                    me.select(me.up('form').getRecord().get(me.name));
                }
            }
        });
    }
});