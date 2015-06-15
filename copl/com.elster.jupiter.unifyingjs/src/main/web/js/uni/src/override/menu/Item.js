Ext.define('Uni.override.menu.Item', {
    override: 'Ext.menu.Item',

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
        arguments[0] = Ext.String.htmlEncode(text);
        this.callParent(arguments);
    }
});