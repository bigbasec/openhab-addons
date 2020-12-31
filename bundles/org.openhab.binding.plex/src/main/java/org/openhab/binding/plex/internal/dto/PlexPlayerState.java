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
package org.openhab.binding.plex.internal.dto;

/**
 * The {@link PlexPlayerState} is the class used to map the
 * player states for the player things.
 *
 * @author Brian Homeyer - Initial contribution
 */
public enum PlexPlayerState {

    Stopped,
    Buffering,
    Playing,
    Paused;

    public static PlexPlayerState of(String state) {
        for (PlexPlayerState playerState : values()) {
            if (playerState.toString().toLowerCase().equals(state)) {
                return playerState;
            }
        }

        return null;
    }
}
