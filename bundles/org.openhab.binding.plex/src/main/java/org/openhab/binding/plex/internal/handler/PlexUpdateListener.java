package org.openhab.binding.plex.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface PlexUpdateListener {
    void onItemStatusUpdate(String sessionKey, String state);
}
