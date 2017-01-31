/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.application({
    name: 'Pandora',
    
    autoCreateViewport: true,
    
    models: ['Station', 'Song'],    
    stores: ['Stations', 'RecentSongs', 'SearchResults'],
    controllers: ['Station', 'Song']
});