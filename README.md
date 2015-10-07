# SecureMessages [![Build Status](https://travis-ci.org/odotopen/SecureMessages.svg?branch=master)](https://travis-ci.org/odotopen/SecureMessages)

[SecureMessages](https://odotopen.org) is an SMS/MMS application that allows you to protect your privacy while communicating with friends.

Using SecureMessages, you can send SMS messages and share media or attachments with complete privacy.

Features:
* Easy. SecureMessages works like any other SMS application. There's nothing to sign up for and no new service your friends need to join.
* Reliable. SecureMessages communicates using encrypted SMS messages. No servers or internet connection required.
* Private. SecureMessages uses the TextSecure encryption protocol to provide privacy for every message, every time.
* Safe. All messages are encrypted locally, so if your phone is lost or stolen, your messages are protected.
* Open Source. SecureMessages is Free and Open Source, enabling anyone to verify its security by auditing the code.


## Project goals

This is a fork of [TextSecure](https://github.com/WhisperSystems/TextSecure) that aims to keep the SMS encryption that TextSecure removed [for a variety of reasons](https://whispersystems.org/blog/goodbye-encrypted-sms/).

SecureMessages focuses on SMS and MMS. This fork aims to:

* Keep SMS/MMS encryption
* Drop Google services dependencies (push messages are not available in SecureMessages)
* Integrate upstream bugfixes and patches from TextSecure

## Migrating from TextSecure to SecureMessages

* In TextSecure, export a plaintext backup. Warning: the backup will **not** be encrypted.
* Install SecureMessages.
* In SecureMessages, import the plaintext backup (this will import the TextSecure backup if no SecureMessages backup is found).
* If TextSecure v2.6.4 or earlier is installed, update or uninstall it so it doesn't conflict (can cause errors with key exchanges).
* Enjoy SecureMessages!

Note: You will have to start new secured sessions with your contacts.

# Contributing

See [CONTRIBUTING.md](https://github.com/odotopen/SecureMessages/blob/master/CONTRIBUTING.md) for how to contribute code, translations, or bug reports.

Instructions on how to setup a development environment and build SecureMessages can be found in [BUILDING.md](https://github.com/odotopen/SecureMessages/blob/master/BUILDING.md).

# Help
## Documentation
Looking for documentation? Check out the wiki of the original project:

https://github.com/WhisperSystems/TextSecure/wiki

# Legal
## Cryptography Notice

This distribution includes cryptographic software. The country in which you currently reside may have restrictions on the import, possession, use, and/or re-export to another country, of encryption software.
BEFORE using any encryption software, please check your country's laws, regulations and policies concerning the import, possession, or use, and re-export of encryption software, to see if this is permitted.
See <http://www.wassenaar.org/> for more information.

## License

Licensed under the GPLv3: http://www.gnu.org/licenses/gpl-3.0.html
