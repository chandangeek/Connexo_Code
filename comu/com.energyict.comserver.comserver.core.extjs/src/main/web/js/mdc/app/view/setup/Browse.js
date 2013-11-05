Ext.define('Mdc.view.setup.Browse', {
    extend: 'Ext.container.Container',
    alias: 'widget.setupBrowse',
    overflowY: 'auto',
    layout: 'fit',
    items: [
        {
            xtype: 'panel',
            cls: 'content-container',
            html: '<a href="#/setup/comservers">comservers</a>'
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});