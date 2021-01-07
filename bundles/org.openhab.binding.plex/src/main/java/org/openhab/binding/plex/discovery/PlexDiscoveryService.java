package org.openhab.binding.plex.discovery;

import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.plex.internal.PlexBindingConstants;
import org.openhab.binding.plex.internal.handler.PlexServerHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlexDiscoveryService extends AbstractDiscoveryService {
    private final PlexServerHandler bridgeHandler;
    private final Logger logger = LoggerFactory.getLogger(PlexDiscoveryService.class);

    public PlexDiscoveryService(PlexServerHandler bridgeHandler) {
        super(PlexBindingConstants.SUPPORTED_THING_TYPES_UIDS, 10, false);
        this.bridgeHandler = bridgeHandler;
    }

    @Override
    protected void startScan() {
        for (String tmpStr : bridgeHandler.getAvailablePlayers()) {
            ThingUID bridgeUID = bridgeHandler.getThing().getUID();
            ThingTypeUID thingTypeUID = PlexBindingConstants.UID_PLAYER;
            ThingUID playerThingUid = new ThingUID(PlexBindingConstants.UID_PLAYER, bridgeUID, tmpStr);

            Map<String, Object> properties = new HashMap<>();
            properties.put(PlexBindingConstants.CONFIG_PLAYER_ID, tmpStr);

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(playerThingUid).withThingType(thingTypeUID)
                    .withProperties(properties).withBridge(bridgeUID)
                    .withRepresentationProperty(PlexBindingConstants.CONFIG_PLAYER_ID)
                    .withLabel("Plex Player Thing (" + tmpStr + ")").build();

            thingDiscovered(discoveryResult);
        }

    }

}
