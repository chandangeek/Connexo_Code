/*
This file is part of Ext JS 4.2

Copyright (c) 2011-2013 Sencha Inc

Contact:  http://www.sencha.com/contact

Commercial Usage
Licensees holding valid commercial licenses may use this file in accordance with the Commercial
Software License Agreement provided with the Software or, alternatively, in accordance with the
terms contained in a written agreement between you and Sencha.

If you are unsure which license is appropriate for your use, please contact the sales department
at http://www.sencha.com/contact.

Build date: 2013-09-18 17:18:59 (940c324ac822b840618a3a8b2b4b873f83a1a9b1)
*/
Ext.define('ExtThemeNeptune.Component', {
    override: 'Ext.Component',

    initComponent: function() {
        this.callParent();

        if (this.dock && this.border === undefined) {
            this.border = false;
        }
    },

    initStyles: function() {
        var me = this,
            border = me.border;

        if (me.dock) {
            // prevent the superclass method from setting the border style.  We want to
            // allow dock layout to decide which borders to suppress.
            me.border = null;
        }
        me.callParent(arguments);
        me.border = border;
    }
});

/*
This file is part of Ext JS 4.2

Copyright (c) 2011-2013 Sencha Inc

Contact:  http://www.sencha.com/contact

Commercial Usage
Licensees holding valid commercial licenses may use this file in accordance with the Commercial
Software License Agreement provided with the Software or, alternatively, in accordance with the
terms contained in a written agreement between you and Sencha.

If you are unsure which license is appropriate for your use, please contact the sales department
at http://www.sencha.com/contact.

Build date: 2013-09-18 17:18:59 (940c324ac822b840618a3a8b2b4b873f83a1a9b1)
*/
Ext.define('ExtThemeNeptune.panel.Panel', {
    override: 'Ext.panel.Panel',
    border: false,
    bodyBorder: false,

    initBorderProps: Ext.emptyFn,

    initBodyBorder: function() {
        // The superclass method converts a truthy bodyBorder into a number and sets
        // an inline border-width style on the body element.  This prevents that from
        // happening if borderBody === true so that the body will get its border-width
        // the stylesheet.
        if (this.bodyBorder !== true) {
            this.callParent();
        }
    }
});

Ext.define('Skyline.panel.Panel', {
    override: 'Ext.panel.Panel',

    beforeRender: function() {
        var me = this;
        this.callParent(arguments);

        if (me.subtitle) {
            this.setSubTitle(me.subtitle);
        }
    },

    /**
     * Set a title for the panel's header. See {@link Ext.panel.Header#title}.
     * @param {String} subtitle
     */
    setSubTitle: function(subtitle) {
        var me = this,
            header = me.header;

        me.subtitle = subtitle;

        if (header) {
            if (header.isHeader) {
                header.setSubTitle(subtitle);
            } else {
                header.subtitle = subtitle;
            }
        } else if (me.rendered) {
            me.updateHeader();
        }
    }
});

Ext.define('Skyline.panel.Header', {
    override: 'Ext.panel.Header',

    headingTpl: [
        // unselectable="on" is required for Opera, other browsers inherit unselectability from the header
        '<span id="{id}-textEl" class="{headerCls}-text {cls}-text {cls}-text-{ui}" unselectable="on"',
        '<tpl if="headerRole">',
        ' role="{headerRole}"',
        '</tpl>',
        '>{title}</span>',
        '<span id="{id}-subTextEl" class="{headerCls}-sub-text {cls}-sub-text {cls}-sub-text-{ui}" unselectable="on"',
        '>{subtitle}</span>'
    ],

    initComponent: function() {
        var me = this;

        this.callParent(arguments);
        me.titleCmp.childEls.push("subTextEl");
    },

    /**
     * Sets the subtitle of the header.
     * @param {String} subtitle The title to be set
     */
    setSubTitle: function(subtitle) {
        var me = this,
            titleCmp = me.titleCmp;

        me.subtitle = subtitle;

        if (titleCmp.rendered) {
            titleCmp.subTextEl.update(me.subtitle || '&#160;');
            titleCmp.updateLayout();
        } else {
            me.titleCmp.on({
                render: function() {
                    me.setSubTitle(subtitle);
                },
                single: true
            });
        }
    }
});

/*
This file is part of Ext JS 4.2

Copyright (c) 2011-2013 Sencha Inc

Contact:  http://www.sencha.com/contact

Commercial Usage
Licensees holding valid commercial licenses may use this file in accordance with the Commercial
Software License Agreement provided with the Software or, alternatively, in accordance with the
terms contained in a written agreement between you and Sencha.

If you are unsure which license is appropriate for your use, please contact the sales department
at http://www.sencha.com/contact.

Build date: 2013-09-18 17:18:59 (940c324ac822b840618a3a8b2b4b873f83a1a9b1)
*/
Ext.define('ExtThemeNeptune.toolbar.Toolbar', {
    override: 'Ext.toolbar.Toolbar',
    usePlainButtons: false,
    border: false
});

/*
This file is part of Ext JS 4.2

Copyright (c) 2011-2013 Sencha Inc

Contact:  http://www.sencha.com/contact

Commercial Usage
Licensees holding valid commercial licenses may use this file in accordance with the Commercial
Software License Agreement provided with the Software or, alternatively, in accordance with the
terms contained in a written agreement between you and Sencha.

If you are unsure which license is appropriate for your use, please contact the sales department
at http://www.sencha.com/contact.

Build date: 2013-09-18 17:18:59 (940c324ac822b840618a3a8b2b4b873f83a1a9b1)
*/
Ext.define('ExtThemeNeptune.layout.component.Dock', {
    override: 'Ext.layout.component.Dock',

    /**
     * This table contains the border removal classes indexed by the sum of the edges to
     * remove. Each edge is assigned a value:
     * 
     *  * `left` = 1
     *  * `bottom` = 2
     *  * `right` = 4
     *  * `top` = 8
     * 
     * @private
     */
    noBorderClassTable: [
        0,                                      // TRBL
        Ext.baseCSSPrefix + 'noborder-l',       // 0001 = 1
        Ext.baseCSSPrefix + 'noborder-b',       // 0010 = 2
        Ext.baseCSSPrefix + 'noborder-bl',      // 0011 = 3
        Ext.baseCSSPrefix + 'noborder-r',       // 0100 = 4
        Ext.baseCSSPrefix + 'noborder-rl',      // 0101 = 5
        Ext.baseCSSPrefix + 'noborder-rb',      // 0110 = 6
        Ext.baseCSSPrefix + 'noborder-rbl',     // 0111 = 7
        Ext.baseCSSPrefix + 'noborder-t',       // 1000 = 8
        Ext.baseCSSPrefix + 'noborder-tl',      // 1001 = 9
        Ext.baseCSSPrefix + 'noborder-tb',      // 1010 = 10
        Ext.baseCSSPrefix + 'noborder-tbl',     // 1011 = 11
        Ext.baseCSSPrefix + 'noborder-tr',      // 1100 = 12
        Ext.baseCSSPrefix + 'noborder-trl',     // 1101 = 13
        Ext.baseCSSPrefix + 'noborder-trb',     // 1110 = 14
        Ext.baseCSSPrefix + 'noborder-trbl'     // 1111 = 15
    ],

    /**
     * The numeric values assigned to each edge indexed by the `dock` config value.
     * @private
     */
    edgeMasks: {
        top: 8,
        right: 4,
        bottom: 2,
        left: 1
    },

    handleItemBorders: function() {
        var me     = this,
            edges  = 0,
            maskT  = 8,
            maskR  = 4,
            maskB  = 2,
            maskL  = 1,
            owner  = me.owner,
            bodyBorder  = owner.bodyBorder,
            ownerBorder = owner.border,
            collapsed   = me.collapsed,
            edgeMasks   = me.edgeMasks,
            noBorderCls = me.noBorderClassTable,
            dockedItemsGen = owner.dockedItems.generation,
            b, borderCls, docked, edgesTouched, i, ln, item, dock, lastValue, mask,
            addCls, removeCls;

        if (me.initializedBorders === dockedItemsGen) {
            return;
        }

        addCls = [];
        removeCls = [];

        borderCls   = me.getBorderCollapseTable();
        noBorderCls = me.getBorderClassTable ? me.getBorderClassTable() : noBorderCls;

        me.initializedBorders = dockedItemsGen;

        // Borders have to be calculated using expanded docked item collection.
        me.collapsed = false;
        docked = me.getDockedItems();
        me.collapsed = collapsed;

        for (i = 0, ln = docked.length; i < ln; i++) {
            item = docked[i];
            if (item.ignoreBorderManagement) {
                // headers in framed panels ignore border management, so we do not want
                // to set "satisfied" on the edge in question
                continue;
            }

            dock = item.dock;
            mask = edgesTouched = 0;
            addCls.length = 0;
            removeCls.length = 0;

            if (dock !== 'bottom') {
                if (edges & maskT) { // if (not touching the top edge)
                    b = item.border;
                } else {
                    b = ownerBorder;
                    if (b !== false) {
                        edgesTouched += maskT;
                    }
                }
                if (b === false) {
                    mask += maskT;
                }
            }
            if (dock !== 'left') {
                if (edges & maskR) { // if (not touching the right edge)
                    b = item.border;
                } else {
                    b = ownerBorder;
                    if (b !== false) {
                        edgesTouched += maskR;
                    }
                }
                if (b === false) {
                    mask += maskR;
                }
            }
            if (dock !== 'top') {
                if (edges & maskB) { // if (not touching the bottom edge)
                    b = item.border;
                } else {
                    b = ownerBorder;
                    if (b !== false) {
                        edgesTouched += maskB;
                    }
                }
                if (b === false) {
                    mask += maskB;
                }
            }
            if (dock !== 'right') {
                if (edges & maskL) { // if (not touching the left edge)
                    b = item.border;
                } else {
                    b = ownerBorder;
                    if (b !== false) {
                        edgesTouched += maskL;
                    }
                }
                if (b === false) {
                    mask += maskL;
                }
            }

            if ((lastValue = item.lastBorderMask) !== mask) {
                item.lastBorderMask = mask;
                if (lastValue) {
                    removeCls[0] = noBorderCls[lastValue];
                }
                if (mask) {
                    addCls[0] = noBorderCls[mask];
                }
            }

            if ((lastValue = item.lastBorderCollapse) !== edgesTouched) {
                item.lastBorderCollapse = edgesTouched;
                if (lastValue) {
                    removeCls[removeCls.length] = borderCls[lastValue];
                }
                if (edgesTouched) {
                    addCls[addCls.length] = borderCls[edgesTouched];
                }
            }

            if (removeCls.length) {
                item.removeCls(removeCls);
            }
            if (addCls.length) {
                item.addCls(addCls);
            }

            // mask can use += but edges must use |= because there can be multiple items
            // on an edge but the mask is reset per item

            edges |= edgeMasks[dock]; // = T, R, B or L (8, 4, 2 or 1)
        }

        mask = edgesTouched = 0;
        addCls.length = 0;
        removeCls.length = 0;

        if (edges & maskT) { // if (not touching the top edge)
            b = bodyBorder;
        } else {
            b = ownerBorder;
            if (b !== false) {
                edgesTouched += maskT;
            }
        }
        if (b === false) {
            mask += maskT;
        }

        if (edges & maskR) { // if (not touching the right edge)
            b = bodyBorder;
        } else {
            b = ownerBorder;
            if (b !== false) {
                edgesTouched += maskR;
            }
        }
        if (b === false) {
            mask += maskR;
        }

        if (edges & maskB) { // if (not touching the bottom edge)
            b = bodyBorder;
        } else {
            b = ownerBorder;
            if (b !== false) {
                edgesTouched += maskB;
            }
        }
        if (b === false) {
            mask += maskB;
        }

        if (edges & maskL) { // if (not touching the left edge)
            b = bodyBorder;
        } else {
            b = ownerBorder;
            if (b !== false) {
                edgesTouched += maskL;
            }
        }
        if (b === false) {
            mask += maskL;
        }

        if ((lastValue = me.lastBodyBorderMask) !== mask) {
            me.lastBodyBorderMask = mask;
            if (lastValue) {
                removeCls[0] = noBorderCls[lastValue];
            }
            if (mask) {
                addCls[0] = noBorderCls[mask];
            }
        }

        if ((lastValue = me.lastBodyBorderCollapse) !== edgesTouched) {
            me.lastBodyBorderCollapse = edgesTouched;
            if (lastValue) {
                removeCls[removeCls.length] = borderCls[lastValue];
            }
            if (edgesTouched) {
                addCls[addCls.length] = borderCls[edgesTouched];
            }
        }

        if (removeCls.length) {
            owner.removeBodyCls(removeCls);
        }
        if (addCls.length) {
            owner.addBodyCls(addCls);
        }
    },

    onRemove: function (item) {
        var lastBorderMask = item.lastBorderMask;

        if (!item.isDestroyed && !item.ignoreBorderManagement && lastBorderMask) {
            item.lastBorderMask = 0;
            item.removeCls(this.noBorderClassTable[lastBorderMask]);
        }

        this.callParent([item]);
    }
});

/*
This file is part of Ext JS 4.2

Copyright (c) 2011-2013 Sencha Inc

Contact:  http://www.sencha.com/contact

Commercial Usage
Licensees holding valid commercial licenses may use this file in accordance with the Commercial
Software License Agreement provided with the Software or, alternatively, in accordance with the
terms contained in a written agreement between you and Sencha.

If you are unsure which license is appropriate for your use, please contact the sales department
at http://www.sencha.com/contact.

Build date: 2013-09-18 17:18:59 (940c324ac822b840618a3a8b2b4b873f83a1a9b1)
*/
Ext.define('ExtThemeNeptune.container.ButtonGroup', {
    override: 'Ext.container.ButtonGroup',
    usePlainButtons: false
});

Ext.define('Skyline.window.MessageBox', {
    override: 'Ext.window.MessageBox',
    shadow: false,

    reconfigure: function (cfg) {
        if (((typeof cfg) != 'undefined') && cfg.ui) {
            this.ui = cfg.ui;
        }
        this.callParent(arguments);
    },

    initComponent: function () {
        var me = this,
            title = me.title;

        me.title = null;
        this.callParent(arguments);
        this.topContainer.padding = 0;

        me.titleComponent = new Ext.panel.Header({
            title: title
        });
        me.promptContainer.insert(0, me.titleComponent);
    },

    /**
     * Set a title for the panel's header. See {@link Ext.panel.Header#title}.
     * @param {String} newTitle
     */
    setTitle: function (newTitle) {
        var me = this,
            header = me.titleComponent;

        if (header) {
            var oldTitle = header.title;
        }

        if (header) {
            if (header.isHeader) {
                header.setTitle(newTitle);
            } else {
                header.title = newTitle;
            }
        }
        else if (me.rendered) {
            me.updateHeader();
        }

        me.fireEvent('titlechange', me, newTitle, oldTitle);
    }
}, function () {
    /**
     * @class Ext.MessageBox
     * @alternateClassName Ext.Msg
     * @extends Ext.window.MessageBox
     * @singleton
     * Singleton instance of {@link Ext.window.MessageBox}.
     */
    Ext.MessageBox = Ext.Msg = new this();
});

Ext.define('Skyline.form.field.Text', {
    override: "Ext.form.field.Text",
    labelAlign: 'right',
    labelPad: 15,
    msgTarget: 'under',
    blankText: 'This is a required field'
});

Ext.define('Skyline.form.field.Base', {
    override: "Ext.form.field.Base",
    labelAlign: 'right',
    labelPad: 15,
    msgTarget: 'under',
    blankText: 'This is a required field',
    getLabelCls: function () {
        var labelCls = this.labelCls;
        if (this.required) {
            labelCls += ' ' + 'uni-form-item-label-required';
        }

        return labelCls;
    },
    initComponent: function() {
        this.callParent(arguments);
    }
});



Ext.define('Skyline.form.field.FieldContainer', {
    override: "Ext.form.FieldContainer",
    getLabelCls: function () {
        var labelCls = this.labelCls;
        if (this.required) {
            labelCls += ' ' + 'uni-form-item-label-required';
        }
        return labelCls;
    },
    initComponent: function() {
        this.callParent(arguments);
    }
});

Ext.define('Skyline.form.Label', {
    override: 'Ext.form.Label',
    cls: 'x-form-item-label'
});

Ext.define('Skyline.form.Panel', {
    override: 'Ext.form.Panel',
    buttonAlign: 'left',

    initComponent: function() {
        var me = this;
        var width = 100;

        if (me.defaults && me.defaults.labelWidth) {
            width = me.defaults.labelWidth;
        }
        // the case when label align is defined and not left. Than don't move the buttons.
        if (me.defaults
         && me.defaults.labelAlign
         && me.defaults.labelAlign != 'left') {
            width = 0;
        }
        if (me.buttons) {
            me.buttons.splice(0, 0, {
                xtype: 'tbspacer',
                width: width
            })
        }

        me.callParent(arguments);
    }
});

/*
This file is part of Ext JS 4.2

Copyright (c) 2011-2013 Sencha Inc

Contact:  http://www.sencha.com/contact

Commercial Usage
Licensees holding valid commercial licenses may use this file in accordance with the Commercial
Software License Agreement provided with the Software or, alternatively, in accordance with the
terms contained in a written agreement between you and Sencha.

If you are unsure which license is appropriate for your use, please contact the sales department
at http://www.sencha.com/contact.

Build date: 2013-09-18 17:18:59 (940c324ac822b840618a3a8b2b4b873f83a1a9b1)
*/
Ext.define('ExtThemeNeptune.toolbar.Paging', {
    override: 'Ext.toolbar.Paging',
    defaultButtonUI: 'plain-toolbar',
    
    inputItemWidth: 40
});

/*
This file is part of Ext JS 4.2

Copyright (c) 2011-2013 Sencha Inc

Contact:  http://www.sencha.com/contact

Commercial Usage
Licensees holding valid commercial licenses may use this file in accordance with the Commercial
Software License Agreement provided with the Software or, alternatively, in accordance with the
terms contained in a written agreement between you and Sencha.

If you are unsure which license is appropriate for your use, please contact the sales department
at http://www.sencha.com/contact.

Build date: 2013-09-18 17:18:59 (940c324ac822b840618a3a8b2b4b873f83a1a9b1)
*/
Ext.define('ExtThemeNeptune.picker.Month', {
    override:  'Ext.picker.Month',
    
    // Monthpicker contains logic that reduces the margins of the month items if it detects
    // that the text has wrapped.  This can happen in the classic theme  in certain
    // locales such as zh_TW.  In order to work around this, Month picker measures
    // the month items to see if the height is greater than "measureMaxHeight".
    // In neptune the height of the items is larger, so we must increase this value.
    // While the actual height of the month items in neptune is 24px, we will only 
    // determine that the text has wrapped if the height of the item exceeds 36px.
    // this allows theme developers some leeway to increase the month item size in
    // a neptune-derived theme.
    measureMaxHeight: 36
});

/*
This file is part of Ext JS 4.2

Copyright (c) 2011-2013 Sencha Inc

Contact:  http://www.sencha.com/contact

Commercial Usage
Licensees holding valid commercial licenses may use this file in accordance with the Commercial
Software License Agreement provided with the Software or, alternatively, in accordance with the
terms contained in a written agreement between you and Sencha.

If you are unsure which license is appropriate for your use, please contact the sales department
at http://www.sencha.com/contact.

Build date: 2013-09-18 17:18:59 (940c324ac822b840618a3a8b2b4b873f83a1a9b1)
*/
Ext.define('ExtThemeNeptune.form.field.HtmlEditor', {
    override: 'Ext.form.field.HtmlEditor',
    defaultButtonUI: 'plain-toolbar'
});

Ext.define('Skyline.grid.Panel', {
    override: 'Ext.grid.Panel',
//    border: false,
//    frame: true,
    bodyBorder: true,
    enableColumnHide: false,
    enableColumnMove: false,
    enableColumnResize: false,
    sortableColumns: false,
    collapsible: false,
    selModel: {
        mode: 'SINGLE'
    }
});

Ext.define('Skyline.view.Table', {
    override: 'Ext.view.Table',
    bodyBorder: true
//    ,
//    scroll: true,
//    scrollbarTpl: '<div class="scrollbar"><div class="up"><span class="arrow-up"></span></div><div class="down"><span class="arrow-down"></span></div><div class="track"><div class="thumb"></div></div></div>',
//
//    // todo: refactor this
//    listeners: {
////        refresh: function () {
////            var body = this.getEl().parent('.x-grid-body');
////            body.update(body.getHTML() + this.scrollbarTpl);
////            if (body.down('.x-grid-view')) {
////                body.down('.x-grid-view').addCls('viewport');
////
////                this.getEl().down('.x-grid-table').addCls('overview');
////
////                var $scrollbar = body.dom,
////                    scrollbar  = tinyscrollbar($scrollbar, {
////                        trackSize: body.down('.track').dom.offsetHeight
////                    });
////            }
////
////        }
//    }
});

/*
This file is part of Ext JS 4.2

Copyright (c) 2011-2013 Sencha Inc

Contact:  http://www.sencha.com/contact

Commercial Usage
Licensees holding valid commercial licenses may use this file in accordance with the Commercial
Software License Agreement provided with the Software or, alternatively, in accordance with the
terms contained in a written agreement between you and Sencha.

If you are unsure which license is appropriate for your use, please contact the sales department
at http://www.sencha.com/contact.

Build date: 2013-09-18 17:18:59 (940c324ac822b840618a3a8b2b4b873f83a1a9b1)
*/
Ext.define('ExtThemeNeptune.panel.Table', {
    override: 'Ext.panel.Table',
    bodyBorder: true
});

/*
This file is part of Ext JS 4.2

Copyright (c) 2011-2013 Sencha Inc

Contact:  http://www.sencha.com/contact

Commercial Usage
Licensees holding valid commercial licenses may use this file in accordance with the Commercial
Software License Agreement provided with the Software or, alternatively, in accordance with the
terms contained in a written agreement between you and Sencha.

If you are unsure which license is appropriate for your use, please contact the sales department
at http://www.sencha.com/contact.

Build date: 2013-09-18 17:18:59 (940c324ac822b840618a3a8b2b4b873f83a1a9b1)
*/
Ext.define('ExtThemeNeptune.grid.RowEditor', {
    override: 'Ext.grid.RowEditor',
    buttonUI: 'default-toolbar'
});


/*
This file is part of Ext JS 4.2

Copyright (c) 2011-2013 Sencha Inc

Contact:  http://www.sencha.com/contact

Commercial Usage
Licensees holding valid commercial licenses may use this file in accordance with the Commercial
Software License Agreement provided with the Software or, alternatively, in accordance with the
terms contained in a written agreement between you and Sencha.

If you are unsure which license is appropriate for your use, please contact the sales department
at http://www.sencha.com/contact.

Build date: 2013-09-18 17:18:59 (940c324ac822b840618a3a8b2b4b873f83a1a9b1)
*/
Ext.define('ExtThemeNeptune.grid.column.RowNumberer', {
    override: 'Ext.grid.column.RowNumberer',
    width: 25
});

/*
This file is part of Ext JS 4.2

Copyright (c) 2011-2013 Sencha Inc

Contact:  http://www.sencha.com/contact

Commercial Usage
Licensees holding valid commercial licenses may use this file in accordance with the Commercial
Software License Agreement provided with the Software or, alternatively, in accordance with the
terms contained in a written agreement between you and Sencha.

If you are unsure which license is appropriate for your use, please contact the sales department
at http://www.sencha.com/contact.

Build date: 2013-09-18 17:18:59 (940c324ac822b840618a3a8b2b4b873f83a1a9b1)
*/
Ext.define('ExtThemeNeptune.resizer.Splitter', {
    override: 'Ext.resizer.Splitter',
    size: 8
});

/*
This file is part of Ext JS 4.2

Copyright (c) 2011-2013 Sencha Inc

Contact:  http://www.sencha.com/contact

Commercial Usage
Licensees holding valid commercial licenses may use this file in accordance with the Commercial
Software License Agreement provided with the Software or, alternatively, in accordance with the
terms contained in a written agreement between you and Sencha.

If you are unsure which license is appropriate for your use, please contact the sales department
at http://www.sencha.com/contact.

Build date: 2013-09-18 17:18:59 (940c324ac822b840618a3a8b2b4b873f83a1a9b1)
*/
Ext.define('ExtThemeNeptune.menu.Menu', {
    override: 'Ext.menu.Menu',
    showSeparator: false
});

/*
This file is part of Ext JS 4.2

Copyright (c) 2011-2013 Sencha Inc

Contact:  http://www.sencha.com/contact

Commercial Usage
Licensees holding valid commercial licenses may use this file in accordance with the Commercial
Software License Agreement provided with the Software or, alternatively, in accordance with the
terms contained in a written agreement between you and Sencha.

If you are unsure which license is appropriate for your use, please contact the sales department
at http://www.sencha.com/contact.

Build date: 2013-09-18 17:18:59 (940c324ac822b840618a3a8b2b4b873f83a1a9b1)
*/
Ext.define('ExtThemeNeptune.menu.Separator', {
    override: 'Ext.menu.Separator',
    border: true
});
    

/*
This file is part of Ext JS 4.2

Copyright (c) 2011-2013 Sencha Inc

Contact:  http://www.sencha.com/contact

Commercial Usage
Licensees holding valid commercial licenses may use this file in accordance with the Commercial
Software License Agreement provided with the Software or, alternatively, in accordance with the
terms contained in a written agreement between you and Sencha.

If you are unsure which license is appropriate for your use, please contact the sales department
at http://www.sencha.com/contact.

Build date: 2013-09-18 17:18:59 (940c324ac822b840618a3a8b2b4b873f83a1a9b1)
*/
Ext.define('ExtThemeNeptune.panel.Tool', {
    override: 'Ext.panel.Tool',
    height: 16,
    width: 16
});

/*
This file is part of Ext JS 4.2

Copyright (c) 2011-2013 Sencha Inc

Contact:  http://www.sencha.com/contact

Commercial Usage
Licensees holding valid commercial licenses may use this file in accordance with the Commercial
Software License Agreement provided with the Software or, alternatively, in accordance with the
terms contained in a written agreement between you and Sencha.

If you are unsure which license is appropriate for your use, please contact the sales department
at http://www.sencha.com/contact.

Build date: 2013-09-18 17:18:59 (940c324ac822b840618a3a8b2b4b873f83a1a9b1)
*/
Ext.define('ExtThemeNeptune.tab.Tab', {
    override: 'Ext.tab.Tab',
    border: false
});

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
        textSpan.addCls(me.iconCls ? 'x-btn-tag-text' : 'x-btn-tag-text-noicon');
        closeIconEl.on('click', function(){
            me.fireEvent('closeclick', me);
            me.destroy();
        });
        this.callParent(arguments)
    }
});

Ext.define('Skyline.button.SortItemButton', {
    extend: 'Skyline.button.TagButton',
    alias: 'widget.sort-item-btn',
    name: 'sortitembtn',
    iconCls: 'x-btn-sort-item-asc',
    sortOrder: 'asc'
});

Ext.define('Skyline.button.StepButton', {
    extend: 'Ext.button.Button',
    alias: 'widget.step-button',
    ui: 'step-active'
});

Ext.define('Skyline.menu.NavigationItem', {
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

Ext.define('Skyline.menu.NavigationMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.navigation-menu',
    cls: 'x-navigation-menu',
    defaults: {
        xtype: 'navigation-item'
    },
    floating: false,
    hidden: false,
    activeStep: 1,
    jumpBack: true,
    jumpForward: false,
    listeners: {
        add: function (menu, item, index) {
            item.renderData.index = item.index = ++index;
            this.updateItemCls(index)
        },
        click: function (menu, item) {
            item.index < menu.activeStep ?
                (menu.jumpBack ? menu.moveTo(item.index) : null) :
                (menu.jumpForward ? menu.moveTo(item.index) : null)
        }
    },

    updateItemCls: function (index) {
        var me = this,
            item = me.items.getAt(index - 1);
        item.removeCls(['step-completed', 'step-active', 'step-non-completed']);
        index < me.activeStep ? item.addCls('step-completed') :
            (index > me.activeStep ? item.addCls('step-non-completed') :
                item.addCls('step-active'));
    },

    moveTo: function (step) {
        var me = this;
        me.moveToStep(step);
        me.fireEvent('movetostep', me.activeStep)
    },

    moveToStep: function (step) {
        var me = this,
            stepCount = me.items.getCount();
        if (1 < step < stepCount) {
            me.activeStep = step;
            me.items.each(function (item) {
                var index = item.index;
                me.updateItemCls(index);
            });
        }
    },

    getActiveStep: function () {
        return this.activeStep;
    },

    moveNextStep: function () {
        this.moveToStep(this.activeStep + 1);
    },

    movePrevStep: function () {
        this.moveToStep(this.activeStep - 1);
    }
});

Ext.define('Skyline.panel.FilterToolbar', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.filter-toolbar',
    titlePosition: 'left',
    layout: {
        type: 'hbox',
        align: 'stretch'
    },
    header: false,
    ui: 'filter-toolbar',
    showClearButton: true,

    items: [
        {
            xtype: 'container',
            itemId: 'itemsContainer',
			defaults: {
				margin: '0 8 0 0'
			},
			flex:1,
            items: []
        },
        {
            xtype: 'label',
            itemId: 'emptyLabel',
            hidden: true
        },
        {
            xtype: 'container',
            itemId: 'toolsContainer',
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            dock: 'left'
        }
    ],

    dockedItems: [
        {
            xtype: 'header',
            dock: 'left'
        },
        {	
        	itemId : 'Reset',
            xtype: 'button',
            text: 'Clear all',
            action: 'clear',
            disabled: true,
            dock: 'right'
        }
    ],

    updateContainer: function(container) {
        var count = container.items.getCount();

        !count
            ? this.getEmptyLabel().show()
            : this.getEmptyLabel().hide()
        ;
        this.getClearButton().setDisabled(!count);
    },

    initComponent: function ()
    {
        var me = this;

        this.dockedItems[0].title = me.title;
        this.items[0].items =  me.content;
        this.items[1].text = me.emptyText;
        this.items[2].items = me.tools;

        this.callParent(arguments);

        if (!this.showClearButton) {
            this.getClearButton().hide();
        }

        this.getContainer().on('afterlayout', 'updateContainer', this);
    },

    getContainer: function() {
       return this.down('#itemsContainer')
    },

    getTools: function() {
        return this.down('#toolsContainer')
    },

    getClearButton: function() {
        return this.down('button[action="clear"]')
    },

    getEmptyLabel: function() {
        return this.down('#emptyLabel')
    }
});

Ext.define('Skyline.panel.StepPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.step-panel',
    text: 'Some step text',

    indexText: '12',
    index: null,

    isLastItem: null,
    isFirstItem: null,
    isMiddleItem: null,
    isOneItem: null,

    isActiveStep: null,
    isCompletedStep: null,
    isNonCompletedStep: null,

    state: 'noncompleted',

    layout: {
        type: 'vbox',
        align: 'left'
    },

    states: {
        active: ['step-active', 'step-label-active'],
        completed: ['step-completed', 'step-label-completed'],
        noncompleted: ['step-non-completed', 'step-label-non-completed']
    },

    items: [],

    handler: function () {
    },

    getStepDots: function () {
        return {
            layout: {
                type: 'vbox',
                align: 'left'
            },
            cls: 'x-panel-step-dots',
            items: [
                {
                    xtype: 'box',
                    name: 'bottomdots',
                    cls: 'x-image-step-dots'
                }
            ]
        }
    },


    getStepLabel: function () {
        var me = this;
        return {
            name: 'step-label-side',
            layout: {
                type: 'hbox',
                align: 'middle'
            },
            items: [
                {
                    xtype: 'button',
                    name: 'steppanellabel',
                    text: me.text,
                    cls: 'x-label-step',
                    ui: 'step-label-active',
                    handler: me.handler
                }
            ]
        }
    },

    getStepPanelLayout: function () {
        var me = this;
        return {
            name: 'basepanel',
            layout: {
                type: 'hbox',
                align: 'middle'
            },
            items: [
                {
                    name: 'steppanelbutton',
                    xtype: 'step-button',
                    ui: 'step-active',
                    text: me.indexText,
                    handler: me.handler
                },
                me.getStepLabel()
            ]
        }
    },

    doStepLayout: function () {
        var me = this,
            items = null;
        me.isFirstItem && (items = [me.getStepPanelLayout(), me.getStepDots()]);
        me.isLastItem && (items = [me.getStepDots(), me.getStepPanelLayout()]);
        me.isMiddleItem && (items = [me.getStepDots(), me.getStepPanelLayout(), me.getStepDots()]);
        me.isOneItem && (items = [me.getStepPanelLayout()]);
        me.items = items
    },

    afterRender: function (panel) {
        panel.stepButton = this.down('panel[name=basepanel]');
        panel.stepLabel = this.down();
        console.log( this.stepButton, this.stepLabel);
     //   this.setState(this.state);
    },

    setState: function (state) {
        !state && (this.state = state);
        console.log(this, this.stepButton, this.stepLabel);
        this.stepButton.setUI(this.states[this.state][0]);
        this.stepLabel.setUI(this.states[this.state][1]);
    },

    getState: function(){
        return this.state;
    },

    initComponent: function () {
        var me = this;
        me.doStepLayout();
        me.callParent(arguments)
    }
});

Ext.define('Skyline.ux.window.Notification', {
    override: 'Ext.ux.window.Notification',
    title: false,
    position: 't',
    stickOnClick: false,
    closable: false,
    ui: 'notification'
});

