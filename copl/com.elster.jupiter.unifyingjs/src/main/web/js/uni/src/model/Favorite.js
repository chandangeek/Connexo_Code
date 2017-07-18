/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.model.Favorite', {
    extend: 'Uni.model.ParentVersion',
    fields: ['favorite', 'comment',
        {name: 'creationDate', defaultValue: null}
    ]
});