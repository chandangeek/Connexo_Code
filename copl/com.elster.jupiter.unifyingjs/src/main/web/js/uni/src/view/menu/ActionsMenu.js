/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.menu.ActionsMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.uni-actions-menu',
    plain: true,
    border: false,
    shadow: false,
    minWidth: 20,
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
            menuItems,
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
            menuItems = Ext.Array.clone(me.items);
        } else if (Ext.isArray(me.items.items)) {
            me.items.items = Ext.Array.sort(me.items.items, sort);
            menuItems = Ext.Array.clone(me.items.items);
        }

        // Add separators where needed (without taking the visibility of the menu items into account)
        var separatorIndices = [],
            indexCounter = -1,
            previousSection = -1,
            itemsBeforeSeparator = 0;

        Ext.Array.forEach(menuItems, function(menuItem) {
            indexCounter++;
            if (Ext.isEmpty(menuItem.section)) {
                if (!Ext.isEmpty(previousSection)) {
                    if (itemsBeforeSeparator > 0) {
                        separatorIndices.push(indexCounter);
                        itemsBeforeSeparator = 0;
                    }
                    itemsBeforeSeparator++;
                    previousSection = undefined;
                }
            } else if (menuItem.section !== previousSection) {
                if (itemsBeforeSeparator > 0) {
                    separatorIndices.push(indexCounter);
                    itemsBeforeSeparator = 0;
                }
                itemsBeforeSeparator++;
                previousSection = menuItem.section;
            }
        });

        // Add the separators
        var counter,
            separatorsAdded = 0,
            theMenuItems;

        if (Ext.isArray(me.items)) {
            theMenuItems = me.items;
        } else if (Ext.isArray(me.items.items)) {
            theMenuItems = me.items.items;
        }

        for (counter = 0; counter < separatorIndices.length; counter++) {
            theMenuItems.splice(separatorIndices[counter] + separatorsAdded, 0, { xtype: 'menuseparator', action:'none' });
            separatorsAdded++;
        }
    },

    onBeforeShow: function() {
        var me = this,
            menuItems = Ext.Array.clone(me.items.items),
            visiblesBeforeSeparator = 0,
            lastVisibleSeparator = undefined;

        Ext.Array.forEach(menuItems, function(menuItem) {
            if (menuItem.xtype === 'menuseparator') {
                if (visiblesBeforeSeparator>0) {
                    menuItem.show();
                    lastVisibleSeparator = menuItem;
                    visiblesBeforeSeparator = 0;
                } else {
                    menuItem.hide();
                }
            } else {
                if (!menuItem.hidden) {
                    visiblesBeforeSeparator++;
                }
            }
        });

        if (!Ext.isEmpty(lastVisibleSeparator) && visiblesBeforeSeparator===0) {
            lastVisibleSeparator.hide();
        }
    },

    // This method is copied from the Ext.menu.Menu code
    // + changes
    onClick: function(e) {
        var me = this,
            item;

        if (me.disabled) {
            e.stopEvent();
            return;
        }

        item = (e.type === 'click') ? me.getItemFromEvent(e) : me.activeItem;
        if (item && item.isMenuItem) {
            if (item.xtype === 'menuseparator') {  // <<< added
                e.stopEvent();
                return; // We don't want the menu to fire a 'click' event when clicked on the separator
            } else if (!item.menu || !me.ignoreParentClicks) {
                item.onClick(e);
            } else {
                e.stopEvent();
            }
        }
        // Click event may be fired without an item, so we need a second check
        if (!item || item.disabled) {
            item = undefined;
            return; // <<< added: We don't want the menu to fire a 'click' event without an item
        }
        me.fireEvent('click', me, item, e);
    }
});