/**
 * @class Uni.view.navigation.Logo
 */
Ext.define('Uni.view.navigation.Logo', {
    extend: 'Ext.button.Button',
    alias: 'widget.navigationLogo',
    text: 'Jupiter Meter Operations',
    action: 'home',
    glyph: 'xe002@icomoon',
    scale: 'medium',
    iconAlign: 'left',
    href: '#',
    hrefTarget: '_self',
    cls: 'nav-logo',

    initComponent: function () {
        this.callParent(arguments);
    },

    setLogoTitle: function (title) {
        if (this.rendered) {
            this.setText(title);
        } else {
            this.text = title;
        }
    },

    setLogoGlyph: function (glyph) {
        if (this.rendered) {
            this.setGlyph(glyph);
        } else {
            this.glyph = glyph;
        }
    }
});