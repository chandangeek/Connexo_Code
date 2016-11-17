Ext.define('Uni.view.menu.ActionsMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.uni-actions-menu',
    plain: true,
    border: false,
    shadow: false,
    defaultAlign: 'tr-br?',
    items: [],

    SECTION_ACTION: 1,
    SECTION_EDIT: 2,
    SECTION_VIEW: 3,
    SECTION_REMOVE: 4,

    initComponent: function() {
        var me = this;
        me.sortMenuItems();
        me.callParent(arguments);
        me.mon(me, 'beforeshow', me.onBeforeShow, me);
    },

    sortMenuItems: function() {
        var me = this,
            // Order the menu items by
            // 1. their section (first WITH section, THEN without)
            // 2. their text (when their section equals)
            sort = function(menuItem1, menuItem2) {
                var sectionResult = 0;
                if ( (menuItem1.xtype === 'menuseparator') || (menuItem2.xtype === 'menuseparator') ) {
                    return 0; // no reordering whenever separators are involved
                }
                if (Ext.isEmpty(menuItem1.section)) {
                    if (!Ext.isEmpty(menuItem2.section)) {
                        sectionResult = 1;
                    }
                } else if (Ext.isEmpty(menuItem2.section)) {
                    if (!Ext.isEmpty(menuItem1.section)) {
                        sectionResult = -1;
                    }
                } else {
                    sectionResult = menuItem1.section - menuItem2.section;
                }
                return sectionResult===0 ? menuItem1.text.localeCompare(menuItem2.text) : sectionResult;
            };

        if (Ext.isArray(me.items)) {
            me.items = Ext.Array.sort(me.items, sort);
        } else if (Ext.isArray(me.items.items)) {
            me.items.items = Ext.Array.sort(me.items.items, sort);
        }
    },

    onBeforeShow: function() {
        var me = this,
            menuItems = Ext.Array.clone(me.items.items),
            separatorIndices = [],
            previousSection = -1,
            indexCounter = -1,
            visiblesBeforeSeparator = 0;

        Ext.Array.forEach(menuItems, function(menuItem) {
            indexCounter++;
            var visible = !menuItem.hidden;
            if (visible) {
                if (menuItem.xtype === 'menuseparator') {
                    if (visiblesBeforeSeparator > 0) {
                        menuItem.show();
                        visiblesBeforeSeparator = 0;
                    } else {
                        menuItem.hide();
                    }
                } else if (Ext.isEmpty(menuItem.section)) {
                    if (!Ext.isEmpty(previousSection) && previousSection !== -1 && visiblesBeforeSeparator > 0) {
                        separatorIndices.push(indexCounter);
                        visiblesBeforeSeparator = 0;
                    }
                    visiblesBeforeSeparator++;
                    previousSection = undefined;
                } else {
                    if (menuItem.section !== previousSection) {
                        if (previousSection !== -1 && visiblesBeforeSeparator > 0) {
                            separatorIndices.push(indexCounter);
                            visiblesBeforeSeparator = 0;
                        }
                        visiblesBeforeSeparator++;
                        previousSection = menuItem.section;
                    } else {
                        visiblesBeforeSeparator++;
                    }
                }
            } else {
                if (menuItem.xtype !== 'menuseparator' && menuItem.section !== previousSection) {
                    if (previousSection !== -1 && visiblesBeforeSeparator > 0) {
                        separatorIndices.push(indexCounter);
                        visiblesBeforeSeparator = 0;
                    }
                    previousSection = menuItem.section;
                }
            }
        });
        if (visiblesBeforeSeparator === 0) {
            if (separatorIndices.length > 0) {
                separatorIndices.pop(); // Don't add the last separator
            } else if (menuItems.length>0 && menuItems[menuItems.length-1].xtype === 'menuseparator') { // last element is a separator
                menuItems[menuItems.length-1].hide(); // hide the last item
            }
        }

        // Add the separators where needed
        var counter = 0, separatorsAdded = 0;
        for (counter = 0; counter < separatorIndices.length; counter++) {
            me.insert(separatorIndices[counter] + separatorsAdded, { xtype: 'menuseparator' });
            separatorsAdded++;
        }
    }

});