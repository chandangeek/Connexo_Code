Ext.define('Skyline.panel.Header', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.filter-toolbar',
    titlePosition: 'left',
    layout: 'hbox',
    header: false,
    ui: 'filter-toolbar',

    dockedItems: [
        {
            xtype: 'header',
            title: this.title,
            dock: 'left'
        },
        {
            xtype: 'button',
            text: 'Clear all',
            action: 'clear',
            disabled: true,
            dock: 'right'
        }
    ],

//    constructor: function (config) {
//        config.dockedItems = config.dockedItems || [];
//        config.dockedItems.push();
//        Ext.apply(this, config);
//
//        this.callSuper([config]);
//    }

    initComponent: function() {
        this.dockedItems[0].title = this.title;
        this.callParent();
    }

//    panelToolbarConfig: {
//        headerPosition: 'left'
//    },
//
//    constructor: function(config) {
//        if ('toolbar' == config.ui) {
//            Ext.applyIf(config, this.panelToolbarConfig);
//            Ext.apply(this, config);
//        }
//        this.callSuper([config]);
//    }
});