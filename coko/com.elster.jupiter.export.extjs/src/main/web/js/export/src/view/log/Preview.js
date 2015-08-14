Ext.define('Dxp.view.log.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.dxp-log-preview',
    router: null,
    initComponent: function () {
        var me = this;
        me.items = {
            xtype: 'form',
            itemId: 'log-preview-form',
            defaults: {
                xtype: 'displayfield',
                labelWidth: 200,
                labelAlign: 'right'
            },
            items: [
                {
                    fieldLabel: Uni.I18n.translate('general.name', 'DES', 'Name'),
                    name: 'name',
                    renderer: function (value) {
                        var url = me.router.getRoute('administration/dataexporttasks/dataexporttask').buildUrl();
                        return '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('log.runStartedOn', 'DES', 'Run started on'),
                    itemId: 'run-started-on'
                },
                {
                    fieldLabel: Uni.I18n.translate('general.status', 'DES', 'Status'),
                    name: 'status'
                }
            ]
        };
        me.callParent(arguments);
    }
});
