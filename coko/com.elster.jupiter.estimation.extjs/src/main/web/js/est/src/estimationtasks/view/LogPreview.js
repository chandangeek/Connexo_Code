Ext.define('Est.estimationtasks.view.LogPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.estimationtasks-log-preview',
    router: null,
    initComponent: function () {
        var me = this;
        me.items = {
            xtype: 'form',
            itemId: 'estimationtasks-log-preview-form',
            defaults: {
                xtype: 'displayfield',
                labelWidth: 200,
                labelAlign: 'right'
            },
            items: [
                {
                    fieldLabel: Uni.I18n.translate('general.name', 'EST', 'Name'),
                    name: 'name',
                    renderer: function (value) {
                        var url = me.router.getRoute('administration/estimationtasks/estimationtask').buildUrl();
                        return '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('estimationtasks.log.runStartedOn', 'EST', 'Run started on'),
                    itemId: 'run-started-on'
                },
                {
                    fieldLabel: Uni.I18n.translate('general.status', 'EST', 'Status'),
                    name: 'status'
                }
            ]
        };
        me.callParent(arguments);
    }
});
