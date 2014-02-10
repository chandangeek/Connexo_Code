Ext.application({
    requires: ['Ext.container.Viewport'],
    name: 'Issue Management',

    appFolder: 'app',

    launch: function() {
        Ext.create('Ext.container.Viewport', {
            layout: 'fit',
            items: [
                {
                    xtype: 'panel',
                    title: 'Issue Management',
                    html : 'Issue Management ...'
                }
            ]
        });
    }
});

