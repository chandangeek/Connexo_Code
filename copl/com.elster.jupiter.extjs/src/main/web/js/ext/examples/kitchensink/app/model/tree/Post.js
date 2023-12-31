/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('KitchenSink.model.tree.Post', {
    extend: 'Ext.data.TreeModel',
    idProperty: 'postid',
    fields: [{
        name: "title",
        convert: undefined
    }, {
        name: "threadid",
        convert: undefined
    }, {
        name: "username",
        convert: undefined
    }, {
        name: "userid",
        convert: undefined
    },  {
        name: "dateline",
        type: 'date',
        dateFormat: 'timestamp'
    }, {
        name: "postid",
        convert: undefined
    }, {
        name: "forumtitle",
        convert: undefined
    }, {
        name: "forumid",
        convert: undefined
    }, {
        name: "replycount",
        type: 'int',
        convert: undefined
    }, {
        name: "lastpost",
        dateFormat: 'timestamp',
        convert: undefined
    }, {
        name: "excerpt",
        convert: undefined
    }]
});