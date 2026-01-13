#!/bin/bash
# nfpm post-remove script (works for both deb and rpm)

# Determine if this is a purge (Debian) or complete uninstall (RPM)
IS_PURGE=false
if [[ "$1" == "purge" ]] || [[ "$1" == "0" ]]; then
    IS_PURGE=true
fi

if [[ "$IS_PURGE" == "true" ]]; then
    # Remove service user
    if id -u myproject >/dev/null 2>&1; then
        userdel myproject 2>/dev/null || true
    fi

    # Remove directories
    rm -rf /var/log/myproject
    rm -rf /var/lib/myproject

    # Remove config
    rm -rf /etc/myproject
    rm -f /etc/default/myproject
    rm -f /etc/sysconfig/myproject

    echo "MyProject completely removed"
else
    echo "MyProject removed (config and data preserved)"
fi

# Reload systemd
systemctl daemon-reload 2>/dev/null || true

exit 0
