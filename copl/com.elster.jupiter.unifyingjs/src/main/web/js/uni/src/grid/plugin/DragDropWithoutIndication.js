/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.grid.plugin.DragDropWithoutIndication', {
    extend: 'Ext.grid.plugin.DragDrop',
    alias: 'plugin.gridviewdragdropwithoutindication',

    onViewRender : function(view) {
        var me = this,
            scrollEl;

        if (me.enableDrag) {
            if (me.containerScroll) {
                scrollEl = view.getEl();
            }

            me.dragZone = new Ext.view.DragZone({
                view: view,
                ddGroup: me.dragGroup || me.ddGroup,
                dragText: me.dragText,
                containerScroll: me.containerScroll,
                scrollEl: scrollEl
            });
        }

        if (me.enableDrop) {
            me.dropZone = new Ext.grid.ViewDropZone({
                indicatorHtml: '',
                indicatorCls: '',
                view: view,
                ddGroup: me.dropGroup || me.ddGroup
            });
        }
    }

});