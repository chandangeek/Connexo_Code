/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.menu.NavigationItem', {
    extend: 'Ext.menu.Item',
    alias: 'widget.navigation-item',
    arrowCls: null,
    renderTpl: [
        '<tpl if="plain">',
        '{text}',
        '<tpl else>',
                '<a id="{id}-itemEl"',
                ' class="' + Ext.baseCSSPrefix + 'menu-item-link{childElCls}"',
                ' href="{href}"',
                '<tpl if="hrefTarget"> target="{hrefTarget}"</tpl>',
                ' hidefocus="true"',
                ' unselectable="on"',
                '<tpl if="tabIndex">',
                    ' tabIndex="{tabIndex}"',
                '</tpl>',
                '>',
                '<div role="img" id="{id}-iconEl" class="' + Ext.baseCSSPrefix + 'menu-item-icon {iconCls}',
                    '{childElCls} {glyphCls}" style="<tpl if="icon">background-image:url({icon});</tpl>',
                    '<tpl if="glyph && glyphFontFamily">font-family:{glyphFontFamily};</tpl>">',
                    '<tpl if="glyph">&#{glyph};</tpl>',
                '</div>',
                '<span class="navigation-item-number">{index}</span>',
                '<span id="{id}-textEl" class="' + Ext.baseCSSPrefix + 'menu-item-text" unselectable="on">{text}</span>',
                '</a>',
        '</tpl>'
    ]
});
