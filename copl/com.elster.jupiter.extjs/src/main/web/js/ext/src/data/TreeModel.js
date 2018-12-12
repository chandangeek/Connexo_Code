/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
/**
 * This class is used as a base class from which to derive Models used in Trees.
 */
Ext.define('Ext.data.TreeModel', {
    extend: 'Ext.data.Model',
    requires: [
        'Ext.data.NodeInterface'
    ],

    mixins: {
        queryable: 'Ext.Queryable'
    },

     getRefItems: function() {
         return this.childNodes;
     },

     getRefOwner: function() {
         return this.parentNode;
     }
},
function () {
    Ext.data.NodeInterface.decorate(this);
});