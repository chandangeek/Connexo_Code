Ext.define('Skyline.button.TagButton', {
    extend: 'Ext.button.Split',
    alias: 'widget.tag-button',
    split: true,
    menu: {},
    ui: 'tag',
    arrowCls: null,
    afterRender: function () {
        var me = this,
            baseSpan = me.getEl().first(),
            textSpan = baseSpan.first().first(),
            closeIcon = baseSpan.createChild({
                tag: 'span',
                cls: 'x-btn-tag-right'
            }),
            closeIconEl = baseSpan.getById(closeIcon.id);
        console.log(me.iconCls || 'x-btn-tag-text-noicon');
        textSpan.addCls(me.iconCls ? 'x-btn-tag-text' : 'x-btn-tag-text-noicon');
        console.log(arguments);
        closeIconEl.on('click', function(){
            me.destroy();
        });
        this.callParent(arguments)
    }
});