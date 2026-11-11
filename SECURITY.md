# Security

This driver runs on a Hubitat Elevation hub and talks to an LG webOS TV on the local network. The goal is that installing it does not widen what an attacker can reach through the hub.

## Reporting a problem

Please report security problems privately to the maintainer, Michael Dailous, rather than in a public issue.

## What the driver can reach

- **Outbound only, to one address:** the TV's IPv4 address from Preferences, on TCP 3000/3001 (websocket and reachability probe) and Wake-on-LAN packets on the LAN. Host names, ports and paths aren't accepted, so traffic can't be redirected to another host by a typo or a crafted value.
- **No inbound endpoints:** the driver opens no listening ports, OAuth endpoints or cloud callbacks.
- **No cloud services.** The only links to other sites are the documentation links on the Preferences page, which open only when clicked. Opening the page loads nothing from other sites.

## Secrets

| Secret | Where it is kept | Notes |
|---|---|---|
| TV pairing key | **Pairing key** preference (password type, masked on the page) | Saved automatically after pairing. Never written to the logs. Clear it and save to pair again. A key in State Variables (from earlier versions, or just issued) is kept there only until the driver reads the same key back from the preference, so a preference Hubitat didn't save can't cause a pairing loop. If Hubitat doesn't save it after 3 attempts, a warning is logged and the key stays in State Variables. |

Hubitat has no encrypted secret store. The key is still kept **unencrypted on the hub and in hub backups**, and anyone with admin access to the hub can read it. Masking keeps it out of the device page and screenshots, not away from a hub administrator.

## Known and accepted risks

- **The TV's certificate isn't verified** (`ignoreSSLIssues`). The TV uses a self-signed certificate, and Hubitat can't pin a specific certificate. Someone able to intercept traffic between the hub and the TV could read the pairing key when it is sent. The real protection is network isolation between untrusted devices and the hub/TV segment; firewall policy belongs to Network & Services Hardening.
- **Commands are available to anything that can control the device:** Hubitat apps you grant the device to, and the Maker API if it exposes the device. Start Web Page and Start Video accept only `http://` and `https://` links.
- **Permissions are granted at pairing.** The driver asks the TV only for the permissions its commands use. A TV paired by an earlier version keeps its broader grant until it is paired again.

## Planned: certificate validation

A private certificate store is planned as part of the network restructuring and hardening work. Once devices present valid certificates from it, certificate checks will be turned on across the drivers (no more `ignoreSSLIssues`), and self-signed certificates will no longer be accepted. Until then, the owner accepts the risk described above.

## Recommended hub settings (owner action)

- Turn on **Hub Login Security**.
- In the **Maker API**, expose only the devices that need it, and keep its access token private.
- Treat **hub backups as secret**: they contain the pairing key.

## Secure coding standard

Every change follows these rules. A line may opt out of one checked rule only with a reviewed marker that gives the reason, for example `// security-check: allow S1 (reason)`. Rules marked *(checked)* are enforced by [`.github/scripts/security-check.sh`](.github/scripts/security-check.sh), which runs on every pull request with the Groovy parse check.

1. **S1 (checked):** Secrets are never stored in `state`; use a `password`-type preference.
2. **S2 (checked):** Secret values are never written to the logs.
3. **S3 (checked):** Raw inbound messages are never logged; log their type, ID and field names instead.
4. **S4 (checked):** No credentials are hard-coded.
5. **S5 (checked):** Pages never load remote resources (images, scripts, styles).
6. **S6 (checked):** Links that open a new tab use `rel="noopener noreferrer"`.
7. **S7 (checked):** Debug logging is off by default, and turns itself off after 30 minutes when switched on.
8. **S8 (checked):** Any file that skips certificate checks explains why and points here.
9. **S9 (checked):** Continuity records, keys and `.env` files are never committed.
10. **Validate all input** from users, apps, the TV and the network before use: addresses must be IPv4, URLs must be `http(s)`, numbers must be in range.
11. **Text from outside the hub** (app names, channel names, device names) has angle brackets removed before it is shown in attributes, data values or labels.
12. **Least privilege:** request only the permissions and access the code uses.
13. **Bound everything:** queues, retries, waits and reconnect back-off have limits.
14. **No new attack surface:** no listening ports, OAuth/cloud endpoints, or remote code or content loading.
