#!/bin/bash
# nfpm pre-install script (works for both deb and rpm)
set -e

# Create service user if not exists
if ! id -u myproject >/dev/null 2>&1; then
    # Use /usr/sbin/nologin for Debian, /sbin/nologin for RHEL
    NOLOGIN_SHELL="/usr/sbin/nologin"
    [[ -f /sbin/nologin ]] && NOLOGIN_SHELL="/sbin/nologin"
    useradd --system --no-create-home --shell "$NOLOGIN_SHELL" myproject
fi

exit 0
