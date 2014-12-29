Ext.define('Apr.view.appservers.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.appservers-preview',

    requires: [
        'Apr.view.appservers.PreviewForm'
    ],

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'APR', 'Actions'),
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'appservers-action-menu'
            }
        }
    ],

    items: {
        xtype: 'appservers-preview-form'
    }
});

