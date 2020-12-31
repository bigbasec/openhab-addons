# Plex Binding

This is an OH3 compatible PLEX binding.  This takes a slightly different approach to the PLEX service than the original 1.x-2.x binding, though it serves a very similar purpose.   

## Supported Things

This binding is a Bridge/Thing model for configuration.   The Bridge is the actual connection to the PLEX server, the Things are the machineId's for the various players you might have.   This can be somewhat confusing, but we'll get more into that shortly.   

The following thing types are supported:

| Thing    | ID       | Discovery | Description |
|----------|----------|-----------|-------------|
| Server   | server   | Manual    | Server bridge manages all communication with PLEX server |
| Player   | player  | Manual    | Monitor represents a machineID/Player on the PLEX server |

## Discovery

Discovery is not available, this is all 100% manual install.   

## Binding Configuration

Binding configuration is done though the GUI.   

## Thing Configuration
Bridge : 
Configuration is two sided here, you'll need to have your host IP and port, as well as either a Token or a valid username and password.  

What will happen if you have the username/password only is the binding will then get the token for you.   Simple as that.   

Thing :
This only accepts one parameter, machineID.   Once the machineId is added as a thing, it will then be added when the bridge is parsing through objects that are doing something on the PLEX server.   

## Channels

_Here you should provide information about available channel types, what their meaning is and how they can be used._

_Note that it is planned to generate some part of this based on the XML files within ```src/main/resources/OH-INF/thing``` of your binding._

Server : 
| channel  | type   | description                  |
|----------|--------|------------------------------|
| currentPlayers  | String | Count of currently configured player  |
| currentPlayersActive | String | Count of configured players that are active |

Player : 
| channel  | type   | description                  |
|----------|--------|------------------------------|
| state    | String | Current state of the player object | 
| title    | String | Title of the current playing media | 
| type     | String | The type of media that is currently being played | 
| endtime  | DateTime | The time when the current media will be finished playing | 
| progress | Dimmer | Progress of the currently media | 
| art      | String | URL of the background art for the media being played | 
| thumb      | String | URL of the cover art for the media being played | 

## Full Example

All configuration done through PaperUI. 

## Any custom content here!

Enjoy!   It's a work in progress, updates to come!  
