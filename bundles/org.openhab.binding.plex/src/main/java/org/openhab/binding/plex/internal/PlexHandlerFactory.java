/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.plex.internal;

import static org.openhab.binding.plex.internal.PlexBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.plex.internal.handler.PlexPlayerHandler;
import org.openhab.binding.plex.internal.handler.PlexServerHandler;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link PlexHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Brian Homeyer - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.plex", service = ThingHandlerFactory.class)
public class PlexHandlerFactory extends BaseThingHandlerFactory {

    private final HttpClient httpClient;
    private final PlexStateDescriptionOptionProvider stateDescriptionProvider;

    @Activate
    public PlexHandlerFactory(final @Reference HttpClientFactory httpClientFactory,
            final @Reference PlexStateDescriptionOptionProvider provider) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.stateDescriptionProvider = provider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (SUPPORTED_SERVER_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new PlexServerHandler((Bridge) thing, httpClient, stateDescriptionProvider);
        } else if (SUPPORTED_PLAYER_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new PlexPlayerHandler(thing);
        }
        return null;
    }
}
