# Blender Motion Control - App

An application that can control objects in a Blender scene from the motion of android devices, in real time.

Requires a computer running Blender with [BMC - Addon](https://github.com/pixup1/BMC-Addon).

## Installation

Get the APK file from the latest release and install it on an Android device.

## Usage

Once the app is connected to [the Blender addon](https://github.com/pixup1/BMC-Addon), it will directly be able to control objects in the scene, set through the addon's interface (for more details, see the README in that repo).

### Connection

The device must be connected to the **same network** as the computer running the addon.

On the main page, click "**Connect to addon**" and scan the addon's QR code. The address can also be entered manually, and the previously connected hosts are also remembered.

### Settings

The application will always get orientation from the phone's gyroscope, but the position can be estimated by either accelerometer or AR camera processing (*not implemented yet*). The accelerometer is **very imprecise**, to the point where I actually doubt its usefulness, but the option is there.

To handle the rotation data, the default mode gives a "**Lock Rotation**" button, which can be held to reposition the device without rotating the Blender object.

Alternatively, there is an "**Absolute Rotation**" mode. This one sends the true rotation data, and thus does not give the "Lock Rotation" option. By clicking the three dots next to the option in the settings, the phone's local space can be matched with the blender object's. For instance, when controlling a camera, you might set the phone's top axis to X- and the right one to Y+.
