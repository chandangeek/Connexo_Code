Ext.define('Dsh.view.Viewport', {
    extend: 'Ext.container.Viewport',
    requires: [
        'Ext.layout.container.Fit',
        'Dsh.view.Main'
    ],
    layout: {
        type: 'fit'
    },
    items: [
        {
            xtype: 'app-main'
        }
    ]
});