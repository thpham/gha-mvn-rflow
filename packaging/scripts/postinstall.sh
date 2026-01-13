#!/bin/bash
# nfpm post-install script (works for both deb and rpm)
set -e

# Check for Java 17+ and warn if not found
JAVA_VERSION=""
if command -v java &>/dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
fi

if [[ -z "${JAVA_VERSION}" ]] || [[ "${JAVA_VERSION}" -lt 17 ]]; then
    echo ""
    echo "WARNING: Java 17+ is required but not found!"
    echo ""
    echo "Please install Java before starting the service:"
    echo "  Debian/Ubuntu: apt install openjdk-21-jre-headless"
    echo "  RHEL/Rocky:    dnf install java-21-openjdk-headless"
    echo "  Fedora:        dnf install java-21-openjdk-headless"
    echo ""
fi

# Handle RHEL/CentOS sysconfig convention
if [[ -d /etc/sysconfig ]] && [[ -f /etc/default/myproject ]]; then
    cp /etc/default/myproject /etc/sysconfig/myproject
fi

# Reload systemd and enable service
systemctl daemon-reload
systemctl enable myproject.service

echo ""
echo "=============================================="
echo " MyProject installed successfully!"
echo "=============================================="
echo ""
echo " Start the service:"
echo "   systemctl start myproject"
echo ""
echo " View logs:"
echo "   journalctl -u myproject -f"
echo ""
echo " Edit configuration:"
if [[ -d /etc/sysconfig ]]; then
    echo "   /etc/sysconfig/myproject"
else
    echo "   /etc/default/myproject"
fi
echo "   /etc/myproject/application.yml"
echo ""

exit 0
