#!/usr/bin/env bash
#
# Traffic Generator for Observability Demo
# Generates realistic traffic patterns for metrics, traces, and logs
#
# Usage:
#   ./scripts/generate-traffic.sh              # Run with defaults (60s, 10 req/s)
#   ./scripts/generate-traffic.sh -d 300       # Run for 5 minutes
#   ./scripts/generate-traffic.sh -r 50        # 50 requests per second
#   ./scripts/generate-traffic.sh -u http://api:8080  # Custom base URL
#

set -euo pipefail

# Default configuration
BASE_URL="${BASE_URL:-http://localhost:8080}"
DURATION="${DURATION:-60}"
RATE="${RATE:-10}"
VERBOSE="${VERBOSE:-false}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Parse command line arguments
while getopts "u:d:r:vh" opt; do
  case $opt in
    u) BASE_URL="$OPTARG" ;;
    d) DURATION="$OPTARG" ;;
    r) RATE="$OPTARG" ;;
    v) VERBOSE="true" ;;
    h)
      echo "Usage: $0 [-u base_url] [-d duration_seconds] [-r requests_per_second] [-v] [-h]"
      echo ""
      echo "Options:"
      echo "  -u  Base URL (default: http://localhost:8080)"
      echo "  -d  Duration in seconds (default: 60)"
      echo "  -r  Requests per second (default: 10)"
      echo "  -v  Verbose output"
      echo "  -h  Show this help"
      exit 0
      ;;
    \?)
      echo "Invalid option: -$OPTARG" >&2
      exit 1
      ;;
  esac
done

# Calculate delay between requests (in seconds, supports decimals)
DELAY=$(echo "scale=3; 1 / $RATE" | bc)

# Endpoints to hit with their weights (higher = more frequent)
ENDPOINTS=(
  "/api/v1/health:50"
  "/actuator/health:20"
  "/actuator/info:10"
  "/actuator/prometheus:5"
  "/api-docs:5"
  "/swagger-ui.html:5"
  "/api/v1/chaos?errorRate=20:10"
  "/api/v1/chaos?errorRate=50&delayMs=100:5"
)

# Build weighted endpoint list
WEIGHTED_ENDPOINTS=()
for entry in "${ENDPOINTS[@]}"; do
  endpoint="${entry%%:*}"
  weight="${entry##*:}"
  for ((i=0; i<weight; i++)); do
    WEIGHTED_ENDPOINTS+=("$endpoint")
  done
done

# Statistics
TOTAL_REQUESTS=0
SUCCESS_REQUESTS=0
CLIENT_ERRORS=0
SERVER_ERRORS=0
TOTAL_TIME_MS=0

log_info() {
  echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
  echo -e "${GREEN}[OK]${NC} $1"
}

log_error() {
  echo -e "${RED}[ERROR]${NC} $1"
}

log_warn() {
  echo -e "${YELLOW}[WARN]${NC} $1"
}

# Check if the service is available
check_service() {
  log_info "Checking if service is available at $BASE_URL..."

  local http_code
  http_code=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/actuator/health" 2>/dev/null || echo "000")

  if [[ "$http_code" == "200" ]]; then
    log_success "Service is healthy"
    return 0
  else
    log_error "Service is not available at $BASE_URL (HTTP $http_code)"
    log_info "Make sure the application is running with:"
    echo ""
    echo "  OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4318/v1/traces \\"
    echo "  OTEL_LOGS_EXPORTER=otlp \\"
    echo "  OTEL_EXPORTER_OTLP_LOGS_ENDPOINT=http://localhost:3100/otlp/v1/logs \\"
    echo "  OTEL_SERVICE_NAME=myproject-api \\"
    echo "  PYROSCOPE_ENABLED=true \\"
    echo "  mvn spring-boot:run -pl modules/api"
    echo ""
    return 1
  fi
}

# Make a single request and track statistics (runs synchronously)
make_request() {
  local endpoint="$1"
  local url="${BASE_URL}${endpoint}"

  # Make request and capture timing
  local start_time end_time duration http_code
  start_time=$(date +%s%3N)
  http_code=$(curl -s -o /dev/null -w "%{http_code}" --max-time 5 "$url" 2>/dev/null || echo "000")
  end_time=$(date +%s%3N)
  duration=$((end_time - start_time))

  ((TOTAL_REQUESTS++)) || true
  ((TOTAL_TIME_MS+=duration)) || true

  if [[ "$http_code" =~ ^2 ]]; then
    ((SUCCESS_REQUESTS++)) || true
    if [[ "$VERBOSE" == "true" ]]; then
      log_success "$http_code $endpoint (${duration}ms)"
    fi
  elif [[ "$http_code" =~ ^4 ]]; then
    ((CLIENT_ERRORS++)) || true
    if [[ "$VERBOSE" == "true" ]]; then
      log_warn "$http_code $endpoint (${duration}ms)"
    fi
  else
    ((SERVER_ERRORS++)) || true
    if [[ "$VERBOSE" == "true" ]]; then
      log_error "$http_code $endpoint (${duration}ms)"
    fi
  fi
}

# Get a random endpoint based on weights
get_random_endpoint() {
  local count=${#WEIGHTED_ENDPOINTS[@]}
  local index=$((RANDOM % count))
  echo "${WEIGHTED_ENDPOINTS[$index]}"
}

# Print statistics
print_stats() {
  local avg_time=0
  if [[ $TOTAL_REQUESTS -gt 0 ]]; then
    avg_time=$((TOTAL_TIME_MS / TOTAL_REQUESTS))
  fi

  local actual_rate=0
  if [[ $DURATION -gt 0 ]]; then
    actual_rate=$((TOTAL_REQUESTS / DURATION))
  fi

  echo ""
  echo "═══════════════════════════════════════════════════════════"
  echo "                    Traffic Generation Complete"
  echo "═══════════════════════════════════════════════════════════"
  echo ""
  printf "  %-25s %s\n" "Total Requests:" "$TOTAL_REQUESTS"
  printf "  %-25s %s\n" "Successful (2xx):" "$SUCCESS_REQUESTS"
  printf "  %-25s %s\n" "Client Errors (4xx):" "$CLIENT_ERRORS"
  printf "  %-25s %s\n" "Server Errors (5xx):" "$SERVER_ERRORS"
  printf "  %-25s %s ms\n" "Average Response Time:" "$avg_time"
  printf "  %-25s %s seconds\n" "Duration:" "$DURATION"
  printf "  %-25s %s req/s\n" "Target Rate:" "$RATE"
  printf "  %-25s %s req/s\n" "Actual Rate:" "$actual_rate"
  echo ""
  echo "═══════════════════════════════════════════════════════════"
  echo ""
  log_info "View the results in Grafana at http://localhost:3000"
  echo ""
  echo "  Dashboards to explore:"
  echo "    - Application Overview: RED metrics, request rates"
  echo "    - JVM Performance: Memory, GC, threads"
  echo "    - Traces Explorer: Distributed traces"
  echo "    - Logs Explorer: Log aggregation and search"
  echo "    - Continuous Profiling: CPU/memory flame graphs"
  echo ""
}

# Cleanup on exit
cleanup() {
  print_stats
  exit 0
}

trap cleanup SIGINT SIGTERM

# Main execution
main() {
  echo ""
  echo "═══════════════════════════════════════════════════════════"
  echo "           Observability Traffic Generator"
  echo "═══════════════════════════════════════════════════════════"
  echo ""
  log_info "Configuration:"
  printf "  %-20s %s\n" "Base URL:" "$BASE_URL"
  printf "  %-20s %s seconds\n" "Duration:" "$DURATION"
  printf "  %-20s %s req/s\n" "Rate:" "$RATE"
  printf "  %-20s %s\n" "Verbose:" "$VERBOSE"
  echo ""

  # Check service availability
  if ! check_service; then
    exit 1
  fi

  echo ""
  log_info "Starting traffic generation for ${DURATION}s at ${RATE} req/s..."
  log_info "Press Ctrl+C to stop early and see statistics"
  echo ""

  local end_time=$(($(date +%s) + DURATION))
  local last_progress_time=$(date +%s)

  while [[ $(date +%s) -lt $end_time ]]; do
    # Get random endpoint and make request (synchronously)
    local endpoint
    endpoint=$(get_random_endpoint)
    make_request "$endpoint"

    # Print progress every 10 seconds (non-verbose mode)
    local current_time=$(date +%s)
    if [[ "$VERBOSE" != "true" ]] && [[ $((current_time - last_progress_time)) -ge 10 ]]; then
      local remaining=$((end_time - current_time))
      log_info "Progress: $TOTAL_REQUESTS requests sent, ${remaining}s remaining..."
      last_progress_time=$current_time
    fi

    # Sleep to maintain rate
    sleep "$DELAY" 2>/dev/null || sleep 0.1
  done

  print_stats
}

main "$@"
