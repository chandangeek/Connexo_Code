/**
 * @class Uni.view.navigation.AppCenter
 */
Ext.define('Uni.view.navigation.AppCenter', {
    extend: 'Ext.button.Button',
    xtype: 'uni-nav-appcenter',

    text: '',
    iconCls: 'icon-appcenter',
    cls: Uni.About.baseCssPrefix + 'nav-appcenter',

    menu: {
        xtype: 'menu',
        plain: true,
        showSeparator: false,
        forceLayout: true,
        cls: Uni.About.baseCssPrefix + 'nav-appcenter-menu',
        items: [
            {
                xtype: 'dataview',
                cls: Uni.About.baseCssPrefix + 'nav-appcenter-dataview',
                tpl: [
                    '<div class="handlebar"></div>',
                    '<tpl for=".">',
                    '<a href="{url}">',
                    '<div class="app-item',
                    '<tpl if="isActive"> x-pressed</tpl>',
                    '">',
                    '<div class="icon icon-{icon}">&nbsp;</div>',
                    '<span class="name">{name}</span>',
                    '</div>',
                    '</a>',
                    '</tpl>'
                ],
                itemSelector: 'div.app-item',
                store: 'apps'
            }
        ]
    },

    initComponent: function () {
        this.callParent(arguments);
    }
});