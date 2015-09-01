/**
 * @class Uni.view.search.Quick
 */
Ext.define('Uni.view.search.Quick', {
    extend: 'Ext.container.Container',
    alias: 'widget.searchQuick',
    cls: 'search-quick',

    layout: {
        type: 'hbox',
        align: 'stretch',
        pack: 'end'
    },

    items: [
        {
            xtype: 'container',
            layout: {
                type: 'hbox',
                align: 'middle'
            },
            items: [
                {
                    xtype: 'textfield',
                    itemId: 'searchField',
                    cls: 'search-field',
                    emptyText: Uni.I18n.translate('general.search','UNI','Search')
                }
            ]
        },
        {
            xtype: 'button',
            itemId: 'searchButton',
            cls: 'search-button',
            glyph: 'xe021@icomoon',
            scale: 'small'
        }
    ]
});