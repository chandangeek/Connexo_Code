/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Ext.rtl.view.Table', {
    override: 'Ext.view.Table',

    rtlCellTpl: [
        '<td class="' + Ext.baseCSSPrefix + 'rtl {tdCls}" {tdAttr} {ariaCellAttr}>',
            '<div {unselectableAttr} {ariaCellInnerAttr} class="' + Ext.baseCSSPrefix + 'rtl ' + Ext.baseCSSPrefix + 'grid-cell-inner {innerCls}"',
                ' style="text-align:{[this.getAlign(values.align)]};<tpl if="style">{style}</tpl>">{value}</div>',
        '</td>',
        {
            priority: 0,
            rtlAlign: {
                right: 'left',
                left: 'right',
                center: 'center'
            },
            getAlign: function(align) {
                return this.rtlAlign[align];
            }
        }
    ],

    beforeRender: function() {
        var me = this;

        me.callParent();
        if (me.getHierarchyState().rtl) {
            me.addCellTpl(me.getTpl('rtlCellTpl'));
        }
    },

    getCellPaddingAfter: function(cell) {
        return Ext.fly(cell).getPadding(this.getHierarchyState().rtl ? 'l' : 'r');
    }
});