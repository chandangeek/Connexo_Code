/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.override.ContextOverride', {
    override: 'Ext.layout.Context',

    // Workaround for the ExtJS bug that produces the error "Uncaught TypeError: Cannot read property 'isItemBoxParent' of undefined"
    // cf. https://www.sencha.com/forum/showthread.php?291412-Error-after-upgrade-to-ExtJS-4.2.3

    queueInvalidate: function(item, options) {
        var me = this,
            comp, item, invalidQueue,
            filteredQueue = [],
            i, queueLength;


        // Call the parent method we are overriding
        me.callParent(arguments);

        // MJN Change: we want to ignore Load Masks with no ownerLayout setting
        invalidQueue = me.invalidQueue;
        queueLength = invalidQueue.length;


        for (i = 0; i < queueLength; i++) {
            comp = invalidQueue[i];
            item = comp.item;
            if (item && item.id.indexOf("loadmask-") > -1 && !item.ownerLayout) {
                //apex.debug("ignoring " + item.$className + " from layout refresh");
            } else {
                filteredQueue.push(comp);
            }
        }
        // Set the invalidQueue to our filtered queue to excldue loadMasks that cause exceptions
        me.invalidQueue = filteredQueue;
    }

});