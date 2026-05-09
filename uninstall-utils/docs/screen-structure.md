# Screen-structure JSON (overlay) — Documentation

This document describes the JSON structure used by the `uninstall-utils` overlay files (examples: `vpn - app info screen xiaomi.json`, `overlay - app info screen xiaomi.json`, etc.). These files represent a snapshot of a UI window as a tree of nodes.

## Top-level structure

- meta (object, optional): Collector or device metadata. May include:
  - versionName (string|null)
  - versionCode (integer|null)
  - manufacturer (string|null)
  - model (string|null)

- time (string): Timestamp when the snapshot was taken. Format is flexible; examples: `2025-10-29 21:44:34` or an ISO 8601 string.

- screenAnalysis (object): The main payload describing the captured screen. Required fields:
  - appName (string|null): Detected application name shown on the screen (if any).
  - hasAppName (boolean): True if an app name was detected.
  - isSettingsScreen (boolean): True if this is a system settings screen.
  - nodesCount (integer): Total number of nodes in the `root` tree (for quick heuristics).
  - pkg (string|null): Package name of the window owner (if known), e.g. `com.android.settings`.
  - root (node): Root node of the UI tree.

## Node object (used for `root` and every element in `children`)

Each node represents an accessibility/view element and has the following fields:

- cls (string, required): Android class name of the view (e.g. `android.widget.TextView`).
- text (string|null): The visible text present in the view. Null when none.
- id (string|null): Android resource id, commonly `android:id/message` or `null`.
- desc (string|null): Accessibility content description.
- children (array of node): Child nodes in visual/hierarchy order. May be an empty array.

Notes:
- `children` is recursive; each child is a full node with the same shape.
- Additional fields may exist (the schema allows `additionalProperties` for `meta` and each node), but the schema in `schema/screen-structure.schema.json` documents the main expected fields.

## Example (based on `vpn - app info screen xiaomi.json`)

```json
{
  "meta": {
    "versionName": "v0.0.11",
    "versionCode": 11,
    "manufacturer": "HONOR",
    "model": "ALI-NX1"
  },
  "time": "2025-10-29 21:44:34",
  "screenAnalysis": {
    "appName": "عَيْنًا سَلْسَبِيلًا",
    "hasAppName": true,
    "isSettingsScreen": true,
    "nodesCount": 5,
    "pkg": "com.android.settings",
    "root": {
      "cls": "android.widget.FrameLayout",
      "children": [
        { "cls": "android.widget.TextView", "id": "android:id/alertTitle", "text": "عَيْنًا سَلْسَبِيلًا", "children": [] },
        { "cls": "android.widget.TextView", "id": "android:id/message", "text": "Disconnect this VPN.", "children": [] },
        { "cls": "android.widget.Button", "id": "android:id/button2", "text": "Cancel", "children": [] },
        { "cls": "android.widget.Button", "id": "android:id/button1", "text": "Disconnect", "children": [] }
      ]
    }
  }
}
```

## Usage

- Validate files with the provided JSON Schema (`uninstall-utils/schema/screen-structure.schema.json`) using any JSON Schema validator (AJV, python `jsonschema`, etc.).
- Use `nodesCount` as a quick pre-check; it should match the accumulated node count of the tree when parsed.

## Tips

- If you extend the node representation with additional fields (e.g. bounds, clickable, visible), add them under the node object and update the schema accordingly.
- Keep `cls` normalized (fully-qualified Android view class) for consistent matching in heuristics.

---
Generated on 2025-11-03.
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "Screen Structure (overlay) JSON Schema",
  "description": "Schema for overlay/screen-analysis JSON files used by the uninstall-utils toolset.",
  "type": "object",
  "required": ["meta", "time", "screenAnalysis"],
  "properties": {
    "meta": {
      "type": "object",
      "description": "Optional metadata about the device/collector that produced the file.",
      "properties": {
        "versionName": { "type": ["string", "null"] },
        "versionCode": { "type": ["integer", "null"] },
        "manufacturer": { "type": ["string", "null"] },
        "model": { "type": ["string", "null"] }
      },
      "additionalProperties": true
    },
    "time": {
      "type": "string",
      "description": "Timestamp when the snapshot was taken. Format is flexible (e.g. 'YYYY-MM-DD HH:MM:SS')."
    },
    "screenAnalysis": {
      "type": "object",
      "required": ["appName", "hasAppName", "isSettingsScreen", "nodesCount", "pkg", "root"],
      "properties": {
        "appName": { "type": ["string", "null"], "description": "Detected app name (if any)." },
        "hasAppName": { "type": "boolean", "description": "Whether an app name was detected on the screen." },
        "isSettingsScreen": { "type": "boolean", "description": "Whether the screen belongs to Android Settings or similar system UI." },
        "nodesCount": { "type": "integer", "minimum": 0, "description": "Total node count in the captured tree." },
        "pkg": { "type": ["string", "null"], "description": "Package name of the app that owns the window (if known)." },
        "root": { "$ref": "#/definitions/node" }
      },
      "additionalProperties": true
    }
  },
  "definitions": {
    "node": {
      "type": "object",
      "required": ["cls"],
      "properties": {
        "cls": { "type": "string", "description": "Android view class name, e.g. android.widget.TextView." },
        "text": { "type": ["string", "null"], "description": "Visible text of the node, if any." },
        "id": { "type": ["string", "null"], "description": "Android resource id (sometimes null)." },
        "desc": { "type": ["string", "null"], "description": "Content description / accessibility description (if any)." },
        "children": {
          "type": "array",
          "description": "Child nodes of this node in visual/hierarchy order.",
          "items": { "$ref": "#/definitions/node" },
          "default": []
        }
      },
      "additionalProperties": true
    }
  },
  "additionalProperties": false
}

