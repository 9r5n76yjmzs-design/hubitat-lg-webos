/**
 * LGTV with webOS
 *
 * Maintained by Michael Dailous for webOS TV 23 and later.
 * Originally developed by Dan Danache.
 *
 * @see https://github.com/9r5n76yjmzs-design/hubitat-lg-webos
 */
import groovy.transform.Field

@Field static final String DRIVER_NAME = 'LGTV Remote'
@Field static final String DRIVER_VERSION = '2.0.0'
@Field static final Integer MAX_QUEUE = 100
@Field static final Integer MAX_WAIT_MS = 10000
@Field static final List<String> BUTTONS = [
    'LEFT',
    'RIGHT',
    'UP',
    'DOWN',
    'RED',
    'GREEN',
    'YELLOW',
    'BLUE',
    'CHANNELUP',
    'CHANNELDOWN',
    'VOLUMEUP',
    'VOLUMEDOWN',
    'PLAY',
    'PAUSE',
    'STOP',
    'REWIND',
    'FASTFORWARD',
    'ASTERISK',
    'BACK',
    'EXIT',
    'ENTER',
    'AMAZON',
    'NETFLIX',
    '3D_MODE',
    'AD',             // Audio Description toggle
    'ASPECT_RATIO',   // Quick Settings Menu - Aspect Ratio
    'CC',             // Closed Captions
    'DASH',           // Live TV
    'GUIDE',
    'HOME',           // Home Dashboard
    'INFO',           // Info button
    'INPUT_HUB',      // Home Dashboard
    'LIST',           // Live TV
    'LIVE_ZOOM',      // Live Zoom
    'MAGNIFIER_ZOOM', // Focus Zoom
    'MENU',           // Quick Settings Menu
    'MUTE',
    'MYAPPS',         // Home Dashboard
    'POWER',          // Power button
    'PROGRAM',        // TV Guide
    'QMENU',          // Quick Settings Menu
    'RECENT',         // Home Dashboard - Recent Apps
    'RECORD',
    'SAP',            // Multi Audio Setting
    'SCREEN_REMOTE',  // Screen Remote
    'TELETEXT',
    'TEXTOPTION',
    '0',
    '1',
    '2',
    '3',
    '4',
    '5',
    '6',
    '7',
    '8',
    '9',
]

metadata {
    definition(name:DRIVER_NAME, component:true, namespace:'dailous', author:'Michael Dailous', importUrl:'https://raw.githubusercontent.com/9r5n76yjmzs-design/hubitat-lg-webos/main/component/lgtv-remote.groovy') {
        attribute 'status', 'enum', ['playing', 'stopped']
    }
    command 'clearQueue'
}

// Called when the device is first added
void installed() {
    log_warn 'Installing device ...'

    // Init state
    state.data = 'This device is used by the LGTV with webOS driver and it\'s pretty useless on its own.'
    state.info = 'Move along, nothing to see here!'
    state.queue = []
    utils_sendEvent name:'status', value:'stopped', descriptionText:'Status is stopped', type:'digital'
}

void clearQueue() {
    unschedule()
    state.queue = []
    utils_sendEvent name:'status', value:'stopped', descriptionText:'Status is stopped', type:'digital'
    try { interfaces.webSocket.close() } catch (e) { }
}

void pushButtons(String websocketURL, String buttonSequence) {
    if (!buttonSequence) return

    // Add to queue, up to MAX_QUEUE items, so a long sequence can't keep the hub busy indefinitely
    List queue = state.queue ?: []
    List<String> items = buttonSequence.trim().split(/\s+/) as List
    if (queue.size() + items.size() > MAX_QUEUE) {
        log_warn "Too many TV Remote buttons queued (limit ${MAX_QUEUE}); sequence ignored"
        return
    }
    queue.addAll items
    state.queue = queue

    // Websocket already active; reuse connection
    if ("${device.currentValue('status', true)}" == 'playing') return

    // Start websocket connection
    try { interfaces.webSocket.close() } catch (e) { }
    // The TV's certificate is self-signed and Hubitat can't pin it, so it isn't verified. See SECURITY.md.
    interfaces.webSocket.connect websocketURL, ignoreSSLIssues:true
}

void webSocketStatus(String message) {

    // Connection opened
    if (message == 'status: open') {
        utils_sendEvent name:'status', value:'playing', descriptionText:'Status is playing', type:'digital'
        runInMillis 100, 'processNextQueueItem'
        return
    }

    // Websocket closed
    if (message == 'status: closing') return

    // Not good
    log_warn "▶ webSocketStatus: ${message}"
    utils_sendEvent name:'status', value:'stopped', descriptionText:'Status is stopped', type:'digital'
}

void processNextQueueItem() {
    List queue = state.queue ?: []
    if (!queue?.size()) {
        clearQueue()
        return
    }

    Integer wait = 350
    String item = queue.remove(0)
    state.queue = queue

    // Wait item
    if (item.startsWith('WAIT:')) {
        String ms = item.substring(5)
        if (ms ==~ /\d{1,5}/ && ms.toInteger() <= MAX_WAIT_MS) {
            wait = ms.toInteger()
        } else {
            log_warn "Invalid TV Remote wait: ${item} (use WAIT:0 to WAIT:${MAX_WAIT_MS}). Ignored."
        }
        runInMillis wait, 'processNextQueueItem'
        return
    }

    // Valid button?
    if (!BUTTONS.contains(item)) {
        log_warn "Invalid TV Remote button: ${item}. Ignored."
        runInMillis wait, 'processNextQueueItem'
        return
    }

    // Send item
    log_debug "◀ Sending ${item}"
    interfaces.webSocket.sendMessage "type:button\nname:${item}\n\n"
    runInMillis wait, 'processNextQueueItem'
}

void parse(String description) {
    // Only the size is logged: raw messages from the TV are never written to the logs
    log_debug "▶ Received message (${description?.length() ?: 0} characters)"
}

// ===================================================================================================================
// Logging helpers (something like this should be part of the SDK and not implemented by each driver)
// ===================================================================================================================

private void log_debug(String message) { parent.log_debug message, device.displayName }
private void log_info(String message) { parent.log_info message, device.displayName }
private void log_warn(String message) { parent.log_warn message, device.displayName }

private void utils_sendEvent(Map event) {
    if (event.value == null) return
    if ("${device.currentValue(event.name, true)}" != "${event.value}") {
        log_info "${event.descriptionText} [${event.type}]"
    } else {
        log_debug "${event.descriptionText} [${event.type}]"
    }
    sendEvent event
}
