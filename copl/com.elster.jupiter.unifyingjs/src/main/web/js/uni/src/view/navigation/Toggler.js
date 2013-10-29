Ext.define('Uni.view.navigation.Toggler', {
    extend: 'Ext.button.Button',
    alias: 'widget.navigationToggler',
    itemId: '#navigationToggler',
    enableToggle: true,
    action: 'toggle',
    glyph: 'xf0c9@icomoon',
    scale: 'small',
    cls: 'nav-toggler',
    pressed: true,
    /*
    * Temporary disabled because there is a layout issue when refreshing when the menu is hidden.
    * If the menu is hidden, the menu items do not get sized correctly; also see: Navigation.js
    * */
//    stateful: true,
    stateId: 'nav-toggler',
    stateEvents: [
        'toggle'
    ],

    initComponent: function () {
        this.callParent(arguments);
    },

    getState: function () {
        // Workaround for getting button toggle state to work.
        if (this.enableToggle) {
            var config = {};
            config.pressed = this.pressed;
            return config;
        }
        return null;
    }
});