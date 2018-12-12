/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.override.menu.Item', {
    override: 'Ext.menu.Item',
    htmlEncode: true,

    setHref: function (href, target) {
        this.href = !Ext.isDefined(href) ? '#' : href;
        this.hrefTarget = !Ext.isDefined(target) ? '_self' : target || this.hrefTarget;

        if (Ext.isDefined(this.itemEl)) {
            this.itemEl.set({
                href: this.href,
                hrefTarget: this.hrefTarget
            });
        }
    },

    setText: function (text) {
        arguments[0] = this.htmlEncode ? Ext.String.htmlEncode(text) : text;
        this.callParent(arguments);
    }
});