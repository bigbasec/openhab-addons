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
package org.openhab.binding.plex.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.plex.internal.PlexBindingConstants;
import org.openhab.binding.plex.internal.config.PlexPlayerConfiguration;
import org.openhab.binding.plex.internal.dto.MediaContainer.Video;
import org.openhab.binding.plex.internal.dto.PlexPlayerState;
import org.openhab.binding.plex.internal.dto.PlexSession;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PlexBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Brian Homeyer - Initial contribution
 */
@NonNullByDefault
public class PlexPlayerHandler extends BaseThingHandler {
    private @NonNullByDefault({}) String playerID;

    private @Nullable PlexServerHandler bridgeHandler;
    private PlexSession currentSessionData;
    private boolean foundInSession;

    private final Logger logger = LoggerFactory.getLogger(PlexPlayerHandler.class);

    public PlexPlayerHandler(Thing thing) {
        super(thing);
        currentSessionData = new PlexSession();
    }

    /**
     * Initialize the player thing, check the bridge status and hang out waiting
     * for the session data to get polled.
     */
    @Override
    public void initialize() {
        PlexPlayerConfiguration config = getConfigAs(PlexPlayerConfiguration.class);
        currentSessionData = new PlexSession();
        foundInSession = false;
        playerID = config.playerID;
        Bridge bridge = getBridge();
        ThingStatus bridgeStatus = bridge != null ? bridge.getStatus() : null;
        boolean bridgeOnline = bridgeStatus == ThingStatus.ONLINE;
        if (bridgeOnline) {
            bridgeHandler = (PlexServerHandler) getBridge().getHandler();
            if (bridgeHandler != null) {
                updateStatus(ThingStatus.UNKNOWN);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    /**
     * Currently readonly, but this will handle events back from the channels at some point
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Readonly, we don't have any channels
        // logger.warn("Handling command '{}' for {}", command, channelUID);
    }

    /**
     * This is really just to set these all back to false so when we refresh the data it's
     * updated for Power On/Off. This is only called from the Server Bridge.
     *
     * @param foundInSession Will always be false, so this can probably be changed.
     */
    public void setFoundInSession(boolean foundInSession) {
        this.foundInSession = foundInSession;
    }

    /**
     * Called when this thing gets it's configuration changed.
     */
    @Override
    public void thingUpdated(Thing thing) {
        dispose();
        this.thing = thing;
        initialize();
    }

    /**
     * Refreshes all the data from the session XML call. This is called from the bridge
     *
     * @param sessionData The Video section of the XML(which is what pertains to the player)
     */
    public void refreshSessionData(Video sessionData) {
        currentSessionData.setState(PlexPlayerState.of(sessionData.getPlayer().getState()));
        currentSessionData.setDuration(sessionData.getMedia().getDuration());
        currentSessionData.setMachineIdentifier(sessionData.getPlayer().getMachineIdentifier());
        currentSessionData.setViewOffset(sessionData.getViewOffset());
        currentSessionData.setTitle(sessionData.getTitle());
        currentSessionData.setType(sessionData.getType());
        currentSessionData.setThumb(sessionData.getThumb());
        currentSessionData.setArt(sessionData.getArt());
        currentSessionData.setLocal(sessionData.getPlayer().getLocal());
        foundInSession = true;
        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * Updates the channel states to match reality.
     */
    public void updateChannels() {
        updateState(new ChannelUID(getThing().getUID(), PlexBindingConstants.CHANNEL_PLAYER_STATE),
                new StringType(String.valueOf(foundInSession ? currentSessionData.getState() : "Stopped")));
        updateState(new ChannelUID(getThing().getUID(), PlexBindingConstants.CHANNEL_PLAYER_POWER),
                new StringType(String.valueOf(foundInSession ? "ON" : "OFF")));
        updateState(new ChannelUID(getThing().getUID(), PlexBindingConstants.CHANNEL_PLAYER_TITLE),
                new StringType(String.valueOf(currentSessionData.getTitle())));
        updateState(new ChannelUID(getThing().getUID(), PlexBindingConstants.CHANNEL_PLAYER_TYPE),
                new StringType(String.valueOf(currentSessionData.getType())));
        updateState(new ChannelUID(getThing().getUID(), PlexBindingConstants.CHANNEL_PLAYER_ART),
                new StringType(String.valueOf(currentSessionData.getArt())));
        updateState(new ChannelUID(getThing().getUID(), PlexBindingConstants.CHANNEL_PLAYER_THUMB),
                new StringType(String.valueOf(currentSessionData.getThumb())));
        updateState(new ChannelUID(getThing().getUID(), PlexBindingConstants.CHANNEL_PLAYER_PROGRESS),
                new StringType(String.valueOf(currentSessionData.getProgress())));
        updateState(new ChannelUID(getThing().getUID(), PlexBindingConstants.CHANNEL_PLAYER_ENDTIME),
                new StringType(String.valueOf(currentSessionData.getEndTime())));
    }
}
