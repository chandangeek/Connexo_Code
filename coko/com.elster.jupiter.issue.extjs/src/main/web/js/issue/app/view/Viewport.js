Ext.define('ViewDataCollectionIssues.view.Viewport', {
    extend: 'Ext.container.Viewport',
    requires:[
        'Ext.layout.container.Fit',
        'ViewDataCollectionIssues.view.Main'
    ],

    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    overflowY: 'auto',

    style: {
        backgroundColor: '#9A9A9A'
    },

    items: [{
        xtype: 'app-main',
        border: 0,
        style: {
            backgroundColor: '#9A9A9A'
        },
        margin: '0 0 0 30',
    }]
});
