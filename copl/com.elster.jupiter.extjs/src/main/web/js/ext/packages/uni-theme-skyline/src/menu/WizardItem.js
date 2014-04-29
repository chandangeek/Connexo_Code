Ext.define('Skyline.menu.WizardItem', {
    extend: 'Ext.menu.Item',
    alias: 'widget.wizard-item',
    renderTpl: [
        '<tpl if="plain">',
        '{text}',
        '<tpl else>',
        '<a id="{id}-itemEl"',
        ' class="' + Ext.baseCSSPrefix + 'menu-item-link{childElCls}"',
        ' href="{href}"',
        '<tpl if="hrefTarget"> target="{hrefTarget}"</tpl>',
        ' hidefocus="true"',
        // For most browsers the text is already unselectable but Opera needs an explicit unselectable="on".
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
        '<span id="{id}-textEl" class="' + Ext.baseCSSPrefix + 'menu-item-text" unselectable="on">{text}</span>',
        '{childElCls}"/>',
        '</a>',
        '</tpl>'
    ]
})
