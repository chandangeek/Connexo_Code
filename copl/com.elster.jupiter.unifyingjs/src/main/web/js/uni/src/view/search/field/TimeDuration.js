Ext.define('Uni.view.search.field.TimeDuration', {
    extend: 'Uni.view.search.field.Numeric',
    xtype: 'uni-search-criteria-timeduration',

    getValue: function () {
        var me = this,
            value = this.superclass.getValue.apply(me);

        return value ? value.map(function (v) {
            v.set('criteria', v.get('criteria') + ':' + me.getUnitField().getValue());
            return v;
        }) : null;
    },

    getUnitField: function () {
        return this.menu.down('combobox[valueField=code]');
    },

    initComponent: function () {
        var me = this;

        me.menuConfig = {
            width: 400
        };

        Ext.suspendLayouts();
        me.callParent(arguments);

        me.menu.addDocked({
            xtype: 'toolbar',
            padding: '5 10',
            dock: 'top',
            style: {
                'background-color': '#fff !important'
            },
            items: [
                {
                    xtype: 'checkbox',
                    boxLabel: Uni.I18n.translate('general.noSpecifiedValue', 'UNI', 'No specified value'),
                    disabled: true,
                    width: 150
                },
                '->',
                {
                    xtype: 'combobox',
                    fieldLabel: Uni.I18n.translate('general.unit', 'UNI', 'Unit'),
                    store: 'Uni.property.store.TimeUnits',
                    displayField: 'localizedValue',
                    valueField: 'code',
                    forceSelection: false,
                    editable: false,
                    width: 200,
                    labelWidth: 50,
                    queryMode: 'local',
                    value: 14
                }
            ]
        });

        Ext.resumeLayouts(true);
    }
});