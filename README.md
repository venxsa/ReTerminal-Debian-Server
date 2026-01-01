# Debian Proot Console

Debian Proot Console delivers a KVM-like Debian shell experience entirely in userspace. The app downloads a Debian ARM64 rootfs on first launch, prepares a bundled proot loader, and connects it to a fast PTY-backed terminal so you can work inside Debian without root, Shizuku, or any virtualization APIs.

## How it works
- **Rootfs bootstrap**: On first launch the app downloads the Debian ARM64 rootfs tarball to `files/downloads/rootfs.tar.gz`, validates the size, and extracts it into `files/debian-rootfs` with live progress.
- **Bundled proot**: A proot loader is packaged with the app (native `.so` plus an asset shim) and copied to `files/bin/proot` with executable permissions.
- **Terminal integration**: Termux's `TerminalView` provides a PTY-backed console with ANSI color, scrollback, and interactive input. The proot command binds `/dev`, `/proc`, `/sys`, and `/sdcard` and starts `/bin/bash --login` inside the Debian rootfs with correct locale and DNS (`/etc/resolv.conf` is rewritten to `8.8.8.8`).
- **Process management**: A singleton `ProcessManager` tracks running PTY sessions and can terminate them cleanly.
- **Fix system**: The Fix System action stops every managed proot session, wipes the extracted rootfs, cached tarball, temp state, and resets the terminal to a fresh install state.

## Features
- Kotlin + Compose + Material3 UI
- MVVM state flows for download/extraction/terminal status
- Progress feedback for download and extraction
- PTY-backed Debian bash with ANSI colors, Ctrl+C support, and smooth scrolling
- No KVM, no hypervisors, no Shizuku—pure userspace proot

## GitHub Actions (APK deployer)
A CI workflow builds and publishes APKs you can download directly from GitHub Actions without any signing secrets:
- `./gradlew :app:assembleDebug :app:assembleRelease` on pushes and PRs
- Uploads debug and release APKs as artifacts named with `versionName` and `versionCode`
- On `v*.*.*` tags, the release APK artifact is also attached to a GitHub Release

If you ever want to sign releases with your own keystore, provide `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, and `KEY_PASSWORD` secrets; otherwise the CI falls back to debug signing so artifacts are always produced.

## Development notes
- Rootfs is downloaded from `https://www.dropbox.com/s/zxfg8aosr7zzmg8/arm64-rootfs-20170318T102424Z.tar.gz?dl=1`.
- Rootfs path: `/data/data/<package>/files/debian-rootfs`
- Proot path: `/data/data/<package>/files/bin/proot`
- DNS/hosts are reset during validation to guarantee network access from inside Debian.
- Use **Fix system** if you need a clean reinstall or if a bootstrap step fails.
