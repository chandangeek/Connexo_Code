/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
/**
 * This override adds RTL positioning methods to Ext.dom.Element.
 */
Ext.define('Ext.rtl.dom.Element_position', {
    override: 'Ext.dom.Element',

    _positionTopRight: ['position', 'top', 'right'],

    statics: {
        getXY: function(el) {
            var doc = document,
                bd = doc.body,
                docEl = doc.documentElement,
                leftBorder = 0,
                topBorder = 0,
                ret = [0,0],
                x, box, scroll;

            el = Ext.getDom(el);

            if(el !== doc && el !== bd){
                // IE has the potential to throw when getBoundingClientRect
                // is called on an element not attached to dom
                if (Ext.isIE) {
                    try {
                        box = el.getBoundingClientRect();
                        // In some versions of IE, the documentElement (HTML element)
                        // will have a 2px border that gets included, so subtract it off
                        topBorder = docEl.clientTop || bd.clientTop;
                        leftBorder = docEl.clientLeft || bd.clientLeft;
                    } catch (ex) {
                        box = { left: 0, top: 0 };
                    }
                } else {
                    box = el.getBoundingClientRect();
                }

                doc = Ext.fly(document, '_getXY');
                if (Ext.rootHierarchyState.rtl) {
                    scroll = doc.rtlGetScroll();
                    x = Ext.Element.getViewportWidth() - box.right + scroll.left;
                } else {
                    scroll = doc.getScroll();
                    x = box.left + scroll.left;
                }
                ret = [
                    Math.round(x - leftBorder),
                    Math.round(box.top + scroll.top - topBorder)
                ];
            }
            return ret;
        },

        setXY: function(el, xy) {
            (el = Ext.fly(el, '_setXY')).position();

            var pts = el.translatePoints(xy),
                style = el.dom.style,
                pos;

            style[Ext.rootHierarchyState.rtl ? 'left' : 'right'] = 'auto';
            for (pos in pts) {
                if (!isNaN(pts[pos])) {
                    style[pos] = pts[pos] + 'px';
                }
            }
        }
    },

    getPositioning: function(autoPx){
        var xStyle = Ext.rootHierarchyState.rtl ? 'right' : 'left',
            styles = this.getStyle([xStyle, 'top', 'position', 'z-index']),
            dom = this.dom;

        if(autoPx) {
            if(styles[xStyle] === 'auto') {
                styles[xStyle] = (xStyle === 'left') ? (dom.offsetLeft + 'px') :
                    (dom.offsetParent.offsetWidth - dom.offsetLeft - dom.offsetWidth);
            }
            if(styles.top === 'auto') {
                styles.top = dom.offsetTop + 'px';
            }
        }

        return styles;
    },

    rtlGetLocalX: function() {
        var me = this,
            offsetParent = me.dom.offsetParent,
            x = me.getStyle('right');

        if (!x || x === 'auto') {
            x = 0;
        } else if (me.pxRe.test(x)) {
            x = parseFloat(x);
        } else {
            x = me.getX();
            if (offsetParent) {
                x -= Ext.fly(offsetParent, '_rtlGetLocalX').getX();
            }
        }

        return x;
    },

    rtlGetLocalXY: function() {
        var me = this,
            offsetParent = me.dom.offsetParent,
            style = me.getStyle(['right', 'top']),
            x = style.right,
            y = style.top;

        if (!x || x === 'auto') {
            x = 0;
        } else if (me.pxRe.test(x)) {
            x = parseFloat(x);
        } else {
            x = me.getX();
            if (offsetParent) {
                x -= Ext.fly(offsetParent, '_rtlGetLocalXY').getX();
            }
        }

        if (!y || y === 'auto') {
            y = 0;
        } else if (me.pxRe.test(y)) {
            y = parseFloat(y);
        } else {
            y = me.getY();
            if (offsetParent) {
                y -= Ext.Element.getY(offsetParent);
            }
        }

        return [x, y];
    },

    rtlSetLocalX: function(x) {
        var style = this.dom.style;

        // clear left style just in case it was previously set by setXY/setLocalXY
        style.left = 'auto';
        style.right = (x === null) ? 'auto' : x + 'px';
    },

    rtlSetLocalXY: function(x, y) {
        var style = this.dom.style;

        // clear left style just in case it was previously set by setXY/setLocalXY
        style.left = 'auto';

        if (x && x.length) {
            y = x[1];
            x = x[0];
        }

        if (x === null) {
            style.right = 'auto';
        } else if (x !== undefined) {
            style.right = x + 'px';
        }

        if (y === null) {
            style.top = 'auto';
        } else if (y !== undefined) {
            style.top = y + 'px';
        }
    },

    rtlTranslatePoints: function(x, y) {
        var pos = this.rtlTranslateXY(x, y);

        return {
            right: pos.x,
            top: pos.y
        };
    },

    rtlTranslateXY: function(x, y) {
        var me = this,
            styles = me.getStyle(me._positionTopRight),
            relative = styles.position == 'relative',
            right = parseFloat(styles.right),
            top = parseFloat(styles.top),
            xy = me.getXY(),
            dom = me.dom,
            doc, body, offsetParentWidth, offsetParent;

        if (x && x.length) {
             y = x[1];
             x = x[0];
        }
        if (isNaN(right)) {
            doc = document;
            body = doc.body;
            if (dom === body) {
                // translateXY can sometimes be called on the body element.
                // e.g. in Renderable#afterFirstLayout if the "container" is a viewport
                right = 0;
            } else {
                offsetParent = dom.offsetParent;
                offsetParentWidth = (offsetParent &&
                    offsetParent !== body && offsetParent !== doc.documentElement) ?
                        offsetParent.scrollWidth : Ext.Element.getViewportWidth();
                right = offsetParentWidth - dom.offsetLeft - me.getWidth();
            }
        }
        if (isNaN(top)) {
            top = relative ? 0 : me.dom.offsetTop;
        }
        right = (typeof x == 'number') ? x - xy[0] + right : undefined;
        top = (typeof y == 'number') ? y - xy[1] + top : undefined;
        return {
            x: right,
            y: top
        };
    },

    translatePoints: function(x, y) {
        return Ext.rootHierarchyState.rtl ? this.rtlTranslatePoints(x, y) : 
            this.callParent(arguments);
    },

    translateXY: function(x, y) {
        return Ext.rootHierarchyState.rtl ? this.rtlTranslateXY(x, y) : 
            this.callParent(arguments);
    }
});