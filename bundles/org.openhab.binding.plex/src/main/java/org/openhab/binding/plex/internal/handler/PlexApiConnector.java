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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Base64;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.plex.internal.config.PlexServerConfiguration;
import org.openhab.binding.plex.internal.dto.MediaContainer;
import org.openhab.binding.plex.internal.dto.MediaContainer.Device;
import org.openhab.binding.plex.internal.dto.MediaContainer.Device.Connection;
import org.openhab.binding.plex.internal.dto.User;
import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * The {@link PlexApiConnector} is responsible for communications with the PLEX server
 *
 * @author Brian Homeyer - Initial contribution
 */
@NonNullByDefault
public class PlexApiConnector {
    private static final int REQUEST_TIMEOUT_MS = 2000;
    private static final String TOKEN_HEADER = "X-Plex-Token";
    private static final String SIGNIN_URL = "https://plex.tv/users/sign_in.xml";
    private static final String CLIENT_ID = "928dcjhd-91ka-la91-md7a-0msnan214563";
    private static final String API_URL = "https://plex.tv/api/resources?includeHttps=1";

    private final HttpClient httpClient;
    private final PlexServerConfiguration connProps;
    private final Logger logger = LoggerFactory.getLogger(PlexApiConnector.class);

    private final XStream xStream = new XStream(new StaxDriver());

    public PlexApiConnector(PlexServerConfiguration connProps, HttpClient httpClient) {

        this.connProps = connProps;
        this.httpClient = httpClient;
        setupXstream();
    }

    /**
     * Check to make sure we have a valid token or username/password and that the Api is
     * accessible.
     */
    public void checkConnection() {
        if (!connProps.hasToken()) {
            logger.debug("No X-Token set, trying to get a token from username/password");
            connProps.setToken(getToken());
        } else {
            getApi();
        }
        if (connProps.hasToken()) {
            getApi();
        }
    }

    /**
     * Base configuration for XStream
     */
    private void setupXstream() {
        XStream.setupDefaultSecurity(xStream);
        xStream.allowTypesByWildcard(
                new String[] { User.class.getPackageName() + ".**", MediaContainer.class.getPackageName() + ".**" });
        xStream.setClassLoader(PlexApiConnector.class.getClassLoader());
        xStream.ignoreUnknownElements();
        xStream.processAnnotations(User.class);
        xStream.processAnnotations(MediaContainer.class);
    }

    /**
     * Fetch the XML data and parse it through xStream to get a MediaContainer object
     *
     * @return
     */
    public @Nullable MediaContainer getSessionData() {
        try {
            String url = "http://" + connProps.getHost() + ":" + String.valueOf(connProps.getPort())
                    + "/status/sessions";
            MediaContainer mediaContainer = doHttpRequest("POST", url, getClientHeaders(), MediaContainer.class);
            return mediaContainer;
        } catch (Exception e) {
            logger.warn("An exception occurred while polling the PLEX Server: '{}'", e.getMessage());
            return null;
        }
    }

    /**
     * Assemble the URL to include the Token
     *
     * @param url The url portion that is returned from the sessions call
     * @return the completed url that will be usable
     */
    public String getURL(String url) {
        String artURL = "http://" + connProps.getHost() + ":"
                + String.valueOf(connProps.getPort() + url + "?X-Plex-Token=" + connProps.getToken());
        return artURL;
    }

    /**
     * This method will get an X-Token from the PLEX servers if one is not provided in the bridge config
     *
     * @return
     */
    public String getToken() {
        try {
            String url = SIGNIN_URL;
            String authString = Base64.getEncoder()
                    .encodeToString((connProps.getUsername() + ":" + connProps.getPassword()).getBytes());
            Properties headers = getClientHeaders();
            headers.put("Authorization", "Basic " + authString);
            User user = doHttpRequest("POST", url, headers, User.class);
            if (user != null) {
                logger.warn("PLEX login successful using username/password");
                return user.getAuthenticationToken();
            } else {
                logger.warn("Invalid credentials for Plex account, please check config");
            }
        } catch (Exception e) {
            logger.warn("An exception occurred while fetching auth token:'{}'", e.getMessage());
        }
        return "";
    }

    /**
     * This method will get the Api information from the PLEX servers.
     */
    public void getApi() {
        try {
            String url = API_URL;
            MediaContainer api = doHttpRequest("GET", url, getClientHeaders(), MediaContainer.class);
            if (api != null) {
                for (Device tmpDevice : api.getDevice()) {
                    for (Connection tmpConn : tmpDevice.getConnection()) {
                        if (connProps.host.equals(tmpConn.getAddress())) {
                            connProps.setScheme(tmpConn.getProtocol());
                            logger.debug("Plex Api fetched.  Found configured PLEX server in Api request, applied. ");
                        }
                    }
                }
            } else {
                logger.warn("Unable to locate configured host connection protocol.  Defaulting to HTTP.");
                connProps.setScheme("http");
            }
        } catch (Exception e) {
            logger.warn("An exception occurred while fetching auth token:'{}'", e.getMessage());
        }
    }

    /**
     * Make an HTTP request and return the class object that was used when calling.
     *
     * @param <T> Class being used(dto)
     * @param method GET/POST
     * @param url What URL to call
     * @param headers Additional headers that will be used
     * @param type class type for the XML parsing
     * @return Returns a class object from the data returned by the call
     */
    private <T> T doHttpRequest(String method, String url, Properties headers, Class<T> type) {
        try {
            String response = HttpUtil.executeUrl(method, url, headers, null, null, REQUEST_TIMEOUT_MS);
            @SuppressWarnings("unchecked")
            T obj = (T) xStream.fromXML(response);
            return obj;
        } catch (MalformedURLException e) {
            logger.debug(e.getMessage(), e);
        } catch (IOException e) {
            logger.debug(e.getMessage(), e);
        } catch (Exception e) {
            logger.debug(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Fills in the header information for any calls to PLEX services
     *
     * @return Property headers
     */
    private Properties getClientHeaders() {
        Properties headers = new Properties();
        headers.put(HttpHeader.USER_AGENT, "openHAB / PLEX binding "); // + VERSION);
        headers.put("X-Plex-Client-Identifier", CLIENT_ID);
        headers.put("X-Plex-Product", "openHAB");
        headers.put("X-Plex-Version", "");
        headers.put("X-Plex-Device", "JRE11");
        headers.put("X-Plex-Device-Name", "openHAB");
        headers.put("X-Plex-Provides", "controller");
        headers.put("X-Plex-Platform", "Java");
        headers.put("X-Plex-Platform-Version", "JRE11");
        if (connProps.hasToken()) {
            headers.put(TOKEN_HEADER, connProps.getToken());
        }
        return headers;
    }
}
