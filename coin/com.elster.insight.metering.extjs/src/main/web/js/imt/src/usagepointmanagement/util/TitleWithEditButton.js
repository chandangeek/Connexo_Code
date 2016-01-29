Ext.define('Imt.usagepointmanagement.util.TitleWithEditButton', {
    extend: 'Ext.toolbar.Toolbar',
    alias: 'widget.title-with-edit-button',
    title: null,
    parentContainerId: null,
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
                    top: '8px',
                },
                scope: me,
                handler: me.toEditMode,
                //listeners: {
                //    click: me.toEditMode
                //}
            }
        ];
        me.callParent(arguments);
    },

    toEditMode: function(){
        var me = this;
        console.log(me.parentContainerId);
    }
});