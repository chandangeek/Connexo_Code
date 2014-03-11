Ext.define('Isu.controller.Workspace', {
    extend: 'Ext.app.Controller',

    views: [
        'workspace.Overview'
    ],

    init: function () {
        this.initMenu();
    },

    initMenu: function () {
        var menuItem = Ext.create('Uni.model.MenuItem', {
            text: 'Workspace',
            href: '#/workspace',
            glyph: 'xe01e@icomoon'
        });

        Uni.store.MenuItems.add(menuItem);
    },

    showOverview: function () {
        var widget = Ext.widget('workspace-overview');
        this.getApplication().fireEvent('changecontentevent', widget);
    }
});