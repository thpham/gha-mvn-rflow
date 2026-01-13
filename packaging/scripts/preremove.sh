#!/bin/bash
# nfpm pre-remove script (works for both deb and rpm)
set -e

# Stop service before removal
if systemctl is-active --quiet myproject 2>/dev/null; then
    echo "Stopping myproject service..."
    systemctl stop myproject
fi

# Disable service
systemctl disable myproject.service 2>/dev/null || true

exit 0
