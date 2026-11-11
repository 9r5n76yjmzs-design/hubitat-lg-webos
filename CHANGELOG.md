# LGTV with webOS - Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.0.0] - 2026-11-11
First release of this fork, maintained by Michael Dailous for webOS TV 23 and later. Based on Dan Danache's 1.8.3.

### Breaking
- New driver namespace `dailous` (was `dandanache`). Install the drivers from this repository and switch existing devices to the new **LGTV with webOS** driver.
- The TV address must be a plain IPv4 address; host names are no longer accepted.
- The TV is asked only for the 30 permissions the driver uses (was 57). A TV paired by an earlier version keeps its broader grant until it is paired again.

### Added
- webOS TV 23+ pairing without the blacklisted signed manifest (request the system info before registering, as required on webOS 33.20.66+).
- Pairing key kept in a masked **Pairing key** preference. Keys saved by earlier versions under State Variables move automatically, and the state copy is deleted only after the key is read back from the preference.
- Request/response correlation in debug logs (`DIAG` lines), without logging payloads or keys.
- `SECURITY.md` with the secure coding standard, and CI that runs a security check and a Groovy parse check on every pull request.

### Changed
- Remote power-on is detected with a TCP probe of the websocket port (3001/3000) instead of ICMP ping, so no ICMP firewall rule is needed.
- Log level defaults to Info.
- Start Web Page and Start Video accept only `http://` and `https://` links.
- TV Remote: up to 100 queued buttons and waits; `WAIT` must be 0 to 10000 ms.

### Fixed
- Notifications: the request URIs started with a stray quote.
- A refused firmware-version request (`401 insufficient permissions`) is logged once at info level and no longer requested on every refresh.
- The current-channel `500 Application error` returned when Live TV isn't being watched is reported as "Not watching Live TV" instead of an error.

### Security
- Raw TV messages are never logged; unexpected messages are logged by shape only.
- Angle brackets are removed from TV-supplied text before it is shown on the hub.
- The Preferences page no longer loads a remote image; new-tab links use `rel="noopener noreferrer"`.

### Known limitations
- Acceptance tests LG-06 (power off/on and standby; Wake-on-LAN power-on did not work on the test TV), LG-07 (network drop and recovery) and LG-08 (internet down, home network up) are not complete.
- The TV's self-signed certificate is not verified until a private certificate store is available. See `SECURITY.md`.

## [1.8.3] - 2025-11-12
### Fixed
- Fix registration on WebOS 33.20.66+ - `@ady.adrianu`

## [1.8.2] - 2025-11-06
### Fixed
- Change current activity to `off` when device is turned off - `@ady.adrianu`

## [1.8.1] - 2025-06-12
### Fixed
- Fix situation where websocket remains in `connecting` status - `@mluck`

## [1.8.0] - 2025-05-12
### Changed
- Change the "Ping interval" preference option into a device command, so it can be used in automations - `@hubitrep`

## [1.7.1] - 2025-04-10
### Fixed
- Ignore 404 error for devices that don't have support for `ssap://com.webos.media/getForegroundAppInfo` - `@hubitrep`

## [1.7.0] - 2025-02-20
### Added
- Add "Set Picture" command to control brightness, contrast, color, backlight, and pictureMode - `@Biggsyboi`, `@mluck`

### Fixed
- Fix "mute" attribute for TV that do not support "volumeStatus" - `@mluck`

## [1.6.1] - 2025-02-11
### Fixed
- Fix register new device not working - `@spotty`
- Fix debug log level - `@salieri`

## [1.6.0] - 2025-02-11
### Added
- Add "MediaTransport" commands: play(), pause() and stop()
- Simulate TV Remote control - `@mluck`, `@spotty`

### Fixed
- Delay the `switch = on` Hubitat event until TV is ready to process commands - `@salieri`

## [1.5.2] - 2025-01-27
### Fixed
- Improve the fast-ping mechanism to avoid overlapping connections - `@hubitrep`
- Fix the "Speak" command for webOS 5 - `@hubitrep`

### Changed
- Rename "networkStatus" attribute to "websocket" to avoid the red icon when TV is off - `@hubitrep`

## [1.5.1] - 2025-01-25
### Fixed
- Improve the detection of TV startup using a fast-ping mechanism - `@hubitrep`
- Fix the "Start Video" and "Speak" commands for webOS 5 - `@hubitrep`

## [1.5.0] - 2025-01-25
### Added
- Add "SpeechSynthesis/Speak" command for TTS announcements

### Fixed
- Changed "Set Channel" to accept string channel number - `@hubitrep`

## [1.4.0] - 2025-01-23
### Added
- Add "ImageCapture/Take" command to take a TV screenshot

### Fixed
- Correctly identify event type (digital vs physical)
- Hide errors for unsupported config keys - `@nclark`

## [1.3.0] - 2025-01-23
### Added
- Add "Screen Saver On" and "Screen Saver Off" commands

### Fixed
- Hide errors for unsupported config keys - `@nclark`

## [1.2.0] - 2025-01-21
### Added
- Add "Start Video" command
- Add "Start Web Page" command

## [1.1.2] - 2025-01-21
### Fixed
- Set `switch:off` immediately when the "Off command is sent"

## [1.1.1] - 2025-01-21
### Fixed
- Also use `getMACFromIP()` to determine MAC when sending the WOL message

## [1.1.0] - 2025-01-20
### Changed
- Use the "hello" message to test if the websocket is really open
- Use ping to detect when TV becomes online
- Don't send the big pairing payload when we already have a Pairing Key
- Automatically enable Wake On Lan settings when TV is first paired
- Show a "Well done" toast on TV screen on successfull pairing
- Add "setPictureMode" and "setSoundOutput" commands
- Add ping interval preference

## [1.0.0] - 2025-01-17
### Added
- Initial version
