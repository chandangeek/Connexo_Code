/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Ext.rtl.util.Renderable', {
    override: 'Ext.util.Renderable',

    _rtlCls: Ext.baseCSSPrefix + 'rtl',
    _ltrCls: Ext.baseCSSPrefix + 'ltr',

    // this template should be exactly the same as frameTableTple, except with the order
    // of right and left TD elements switched.
    rtlFrameTableTpl: [
        '{%this.renderDockedItems(out,values,0);%}',
        '<table id="{fgid}Table" class="', Ext.plainTableCls, '" cellpadding="0" role="presentation">',
        '<tbody role="presentation">',
            '<tpl if="top">',
                '<tr role="presentation">',
                    '<tpl if="right"><td id="{fgid}TR" class="{frameCls}-tr {baseCls}-tr {baseCls}-{ui}-tr<tpl for="uiCls"> {parent.baseCls}-{parent.ui}-{.}-tr</tpl>{frameElCls}" role="presentation"></td></tpl>',
                    '<td id="{fgid}TC" class="{frameCls}-tc {baseCls}-tc {baseCls}-{ui}-tc<tpl for="uiCls"> {parent.baseCls}-{parent.ui}-{.}-tc</tpl>{frameElCls}" role="presentation"></td>',
                    '<tpl if="left"><td id="{fgid}TL" class="{frameCls}-tl {baseCls}-tl {baseCls}-{ui}-tl<tpl for="uiCls"> {parent.baseCls}-{parent.ui}-{.}-tl</tpl>{frameElCls}" role="presentation"></td></tpl>',
                '</tr>',
            '</tpl>',
            '<tr role="presentation">',
                '<tpl if="right"><td id="{fgid}MR" class="{frameCls}-mr {baseCls}-mr {baseCls}-{ui}-mr<tpl for="uiCls"> {parent.baseCls}-{parent.ui}-{.}-mr</tpl>{frameElCls}" role="presentation"></td></tpl>',
                '<td id="{fgid}MC" class="{frameCls}-mc {baseCls}-mc {baseCls}-{ui}-mc<tpl for="uiCls"> {parent.baseCls}-{parent.ui}-{.}-mc</tpl>{frameElCls}" role="presentation">',
                    '{%this.applyRenderTpl(out, values)%}',
                '</td>',
                '<tpl if="left"><td id="{fgid}ML" class="{frameCls}-ml {baseCls}-ml {baseCls}-{ui}-ml<tpl for="uiCls"> {parent.baseCls}-{parent.ui}-{.}-ml</tpl>{frameElCls}" role="presentation"></td></tpl>',
            '</tr>',
            '<tpl if="bottom">',
                '<tr role="presentation">',
                    '<tpl if="right"><td id="{fgid}BR" class="{frameCls}-br {baseCls}-br {baseCls}-{ui}-br<tpl for="uiCls"> {parent.baseCls}-{parent.ui}-{.}-br</tpl>{frameElCls}" role="presentation"></td></tpl>',
                    '<td id="{fgid}BC" class="{frameCls}-bc {baseCls}-bc {baseCls}-{ui}-bc<tpl for="uiCls"> {parent.baseCls}-{parent.ui}-{.}-bc</tpl>{frameElCls}" role="presentation"></td>',
                    '<tpl if="left"><td id="{fgid}BL" class="{frameCls}-bl {baseCls}-bl {baseCls}-{ui}-bl<tpl for="uiCls"> {parent.baseCls}-{parent.ui}-{.}-bl</tpl>{frameElCls}" role="presentation"></td></tpl>',
                '</tr>',
            '</tpl>',
        '</tbody></table>',
        '{%this.renderDockedItems(out,values,1);%}'
    ],

    beforeRender: function() {
        var rtl = this.getHierarchyState().rtl;
        if (rtl) {
            this.addCls(this._rtlCls);
        } else if (rtl === false) {
            this.addCls(this._ltrCls);
        }

        this.callParent();
    },

    getFrameTpl: function(table) {
        return (table && this.getHierarchyState().rtl) ?
            this.getTpl('rtlFrameTableTpl') : this.callParent(arguments);
    },

    initRenderData: function() {
        var me = this,
            renderData = me.callParent(),
            rtlCls = me._rtlCls;

        if (rtlCls && me.getHierarchyState().rtl) {
            renderData.childElCls = ' ' + rtlCls;
        }

        return renderData;
    },

    getFrameRenderData: function () {
        var me = this,
            data = me.callParent(),
            rtlCls = me._rtlCls;

        if (rtlCls && me.getHierarchyState().rtl) {
            data.frameElCls = ' ' + rtlCls;
        }

        return data;
    }
});
