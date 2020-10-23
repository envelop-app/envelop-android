# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.3.1] - 2020-10-32
### Fixed
- Login issue with API 30 on Blockstack library

## [1.3.0] - 2020-10-16
### Added
- Blockstack v0.6 SDK, and updated login flow with Blockstack Connect

## [1.2.10] - 2020-09-07

### Fixed
- Protected against potential Nullpointer exceptions on getDate from json object

## [1.2.9] - 2020-09-07

### Fixed
- Error Message for Invalid Username
- Socket Timeout Exceptions and GaiExceptions on unsubscribed RxSingles

## [1.2.8] - 2019-02-05

### Added
- Support for gesture navigation and edge-to-edge rendering

### Fixed
- File bottom-sheet menu position glitches
- API21 crash on FAQ page

## [1.2.7] - 2019-02-03

### Fixed
- Fix API21 crashes with scroll events call
- Fix API21 stretched splash screen

## [1.2.6] - 2019-01-29

### Fixed
- Fix crashes related with the file menu fragment

## [1.2.5] - 2019-01-20

### Fixed
- `Context.startForegroundService` was causing ANRs issues, so it was replaced by `Context.startService()`
- `getRelativeDateTimeStringUpload()` was returning wrong date formats for some Locales

## [1.2.4] - 2019-12-24

### Changed
- Update to blockstack-android 0.5.0

## [1.2.3] - 2019-12-23

### Fixed
- IllegalState crash with bottom sheet fragment

## [1.2.2] - 2019-11-21

### Added
- Donations screen
- FAQ screen

### Changed
- Additional instructions on the initial sign in screen

### Fixed
- Crash when dismissing file bottom menu
- Date format changed to avoid compatibility issues with older Android API versions
- Lint errors and warnings

## [1.2.1] - 2019-08-09

### Changed
- Protect against duplicate doc ids

### Fixed
- Added fallback for browsers that don't finish the login process correctly

## [1.2.0] - 2019-08-05

### Added
- Now all files are encrypted in the storage. You don't need to trust your storage provider to use Envelop.

## [1.1.2] - 2019-07-16

### Changed
- Prepare the app for the next features coming up to the Web and Android apps:
  - Store unknown file data as well, instead of dropping it.
  - Block file editions if the file was uploaded with a more recent version of the spec.

## [1.1.1] - 2019-07-15

### Added
- File upload progress on the share screen
- File sharing recommendations on the share screen

### Fixed
- Handle files without name and/or without extension

## [1.1.0] - 2019-07-12

### Added
- Large file upload - now you can upload files larger than 25 MB, through file partition (seamless)
- Feedback button to quickly send an email to the Envelop team

## [1.0.1] - 2019-06-28

### Added
- PDF file icon

### Changed
- Video file icon.

## [1.0.0] - 2019-06-17

- Initial version
