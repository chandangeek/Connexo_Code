Ext.define('Imt.util.TitleWithEditButton', {
    extend: 'Ext.toolbar.Toolbar',
    alias: 'widget.title-with-edit-button',
    title: null,
    editHandler:null,
    record: null,
    items: [],

    initComponent: function() {
        var me = this;

        me.items = [
            '<label class="x-form-item-label x-form-item-label-top">' + me.title + '</label>',
            {
                xtype: 'button',
                icon: '../mdc/resources/images/pencil.png',
                cls: 'uni-btn-transparent masterfield-btn',
                style: {
                    display: 'inline-block',
                    textDecoration: 'none !important',
                    position: 'absolute',
                    top: '8px'
                },
                scope: me,
                handler: me.editHandler,
                //listeners: {
                //    click: me.toEditMode
                //}
            }
        ];
        me.callParent(arguments);
    },

});