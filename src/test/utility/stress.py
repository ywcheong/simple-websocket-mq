import requests
import time
from concurrent.futures import ThreadPoolExecutor, as_completed

# === Test parameters =====================================================
URL               = "http://localhost:8080/send"
HEADERS           = {"accept": "*/*", "Content-Type": "application/json"}

REQUESTS_PER_SEC  = 1000      # X: desired throughput
TEST_DURATION_SEC = 40       # total runtime
POOL_SIZE         = 50       # threads kept alive and reused
TIMEOUT_SEC       = 5        # per-request timeout
# ========================================================================

def send_once(sec_idx, thread_idx):
    """Send one POST request and return HTTP status (or −1 on error)."""
    payload = {
        "title": f"Stress Test Payload #{sec_idx}-{thread_idx}",
        "body":  "This is a payload from a stress testing.",
        "topics": ["testDefault"]
    }
    try:
        r = requests.post(URL, json=payload, headers=HEADERS, timeout=TIMEOUT_SEC)
        return r.status_code
    except Exception:
        return -1

def main():
    start_time = time.time()
    with ThreadPoolExecutor(max_workers=POOL_SIZE) as executor:
        for sec_idx in range(TEST_DURATION_SEC):
            print(f"--- Second {sec_idx+1} of {TEST_DURATION_SEC} ---")
            cycle_start = time.time()

            # Launch a burst of REQUESTS_PER_SEC tasks with unique payloads
            futures = [
                executor.submit(send_once, sec_idx, thread_idx)
                for thread_idx in range(REQUESTS_PER_SEC)
            ]

            # Wait for this batch to finish (optional: print status codes)
            for f in as_completed(futures):
                status = f.result()
                # print(f"status={status}")  # Uncomment for debugging

            # Sleep the rest of the second to keep an exact RPS
            elapsed = time.time() - cycle_start
            if elapsed < 1.0:
                time.sleep(1.0 - elapsed)

if __name__ == "__main__":
    main()
