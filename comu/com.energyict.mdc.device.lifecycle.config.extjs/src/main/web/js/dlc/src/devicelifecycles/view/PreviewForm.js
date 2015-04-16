Ext.define('Dlc.devicelifecycles.view.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.device-life-cycles-preview-form',
    isOverview: false,
    defaults: {
        xtype: 'displayfield',
        labelWidth: 250
    },
    initComponent: function () {
        var me = this;

        me.items = [
            {
                itemId: 'cycle-name',
                fieldLabel: Uni.I18n.translate('general.name', 'DLC', 'Name'),
                name: 'name'
            },
            {
                itemId: 'number-of-states',
                hidden: !me.isOverview,
                fieldLabel: Uni.I18n.translate('general.states', 'DLC', 'States'),
                name: 'statesCount'
            },
            {
                itemId: 'number-of-transitions',
                hidden: !me.isOverview,
                fieldLabel: Uni.I18n.translate('general.transitions', 'DLC', 'Transitions'),
                name: 'actionsCount'
            },
            {
                xtype: 'fieldcontainer',
                name: 'deviceTypes',
                fieldLabel: Uni.I18n.translate('general.usedBy', 'DLC', 'Used by'),
                items: [
                    {
                        xtype: 'container',
                        itemId: 'used-by',
                        items: [
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});
