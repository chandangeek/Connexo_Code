Ext.define('Dxp.view.log.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.log-preview',
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
                    fieldLabel: Uni.I18n.translate('deviceloadprofiles.name', 'DES', 'Name'),
                    name: 'name',
                    renderer: function (value) {
                        var url = me.router.getRoute('administration/dataexporttasks/dataexporttask').buildUrl({taskId: me.down('#log-preview-form').getRecord().get('id')});
                        return '<a href="' + url + '">' + value + '</a>';
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('log.runStartedOn', 'DES', 'Run started on'),
                    value: 'Tue 01 Oct 2013 at 09:00 AM'
                },
                {
                    fieldLabel: Uni.I18n.translate('general.status', 'DES', 'Status'),
                    value: 'Failed'
                }
            ]
        };
        me.callParent(arguments);
    }
});
