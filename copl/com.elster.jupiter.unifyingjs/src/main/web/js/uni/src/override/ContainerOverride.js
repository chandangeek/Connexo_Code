/**
 * @class Uni.override.ContainerOverride
 */
Ext.define('Uni.override.ContainerOverride', {
    override: 'Ext.container.AbstractContainer',
    requires: [
        'Ext.Array',
        'Uni.Auth'
    ],

    add: function() {
        var me = this,
            args = Ext.Array.slice(arguments),
            items;

        if (args.length == 1 && Ext.isArray(args[0])) {
            items = args[0];
        } else {
            items = args;
        }

        var len = items.length;
        var i=0;
        var allowedItems = [];
        for (; i < len; i++) {
            var item = items[i];
            if (item != null &&
                (typeof item.privileges === 'undefined' ||
                    typeof item.privileges === null ||
                    Uni.Auth.checkPrivileges(item.privileges))) {
                allowedItems.push(item);
            }
        }

        return me.callParent(allowedItems);
    }

});