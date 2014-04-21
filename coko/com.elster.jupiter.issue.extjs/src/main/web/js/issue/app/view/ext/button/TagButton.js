Ext.define('Isu.view.ext.button.TagButton', {
    extend: 'Ext.button.Split',
    alias: 'widget.tag-button',
    width: 150,
    split: true,
    menu: {},
//    ui: 'tag',
    arrowCls: null,
    afterRender: function () {
        var me = this,
            btnEl = this.getEl(),
            baseSpan = btnEl.first(),
            closeIcon = baseSpan.createChild({
                tag: 'span',
                cls: 'x-btn-tag-right'
            }),
            icon = baseSpan.getById(closeIcon.id);
        icon.on('click', function(){
            me.destroy();
        });
        this.callParent(arguments)
    }
});