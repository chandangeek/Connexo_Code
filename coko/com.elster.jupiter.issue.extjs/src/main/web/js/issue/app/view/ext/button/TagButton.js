Ext.define('Isu.view.ext.button.TagButton', {
    extend: 'Ext.button.Split',
    alias: 'widget.tag-button',
    split: true,
    menu: {},
    ui: 'tag',
    arrowCls: null,
    afterRender: function () {
        var me = this,
            btnEl = this.getEl(),
            baseSpan = btnEl.first(),
            textSpan = baseSpan.first().first(),
            closeIcon = baseSpan.createChild({
                tag: 'span',
                cls: 'x-btn-tag-right'
            }),
            closeIconEl = baseSpan.getById(closeIcon.id);
        textSpan.addCls('x-btn-tag-text');
        closeIconEl.on('click', function(){
            me.destroy();
        });
        this.callParent(arguments)
    }
});