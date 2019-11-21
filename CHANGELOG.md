# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
