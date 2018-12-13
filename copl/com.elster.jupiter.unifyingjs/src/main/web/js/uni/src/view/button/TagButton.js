/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.button.TagButton', {
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
        textSpan.addCls(me.iconCls ? 'x-btn-tag-text' : 'x-btn-tag-text-noicon');
        closeIconEl.on('click', function(){
            me.fireEvent('closeclick', me);
            me.destroy();
        });
        this.callParent(arguments)
    }
});