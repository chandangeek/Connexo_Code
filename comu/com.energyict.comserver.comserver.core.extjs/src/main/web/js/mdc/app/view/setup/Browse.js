Ext.define('Mdc.view.setup.Browse', {
    extend: 'Ext.container.Container',
    alias: 'widget.setupBrowse',
    cls: 'content-wrapper',
    overflowY: 'auto',

    items: [
        {
            xtype: 'component',
            cls: 'content-container',
            html: 'booooojaaaaaaaa'
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});