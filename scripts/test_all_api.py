import urllib.request, json, ssl

ctx = ssl.create_default_context()
ctx.check_hostname = False
ctx.verify_mode = ssl.CERT_NONE

# Step 1: Login to get token
data = json.dumps({"username": "admin", "password": "123456"}).encode()
req = urllib.request.Request("http://localhost:18080/api/user/login", data=data, headers={"Content-Type": "application/json"})
resp = urllib.request.urlopen(req, timeout=10)
login_result = json.loads(resp.read().decode())
token = login_result["data"]["token"]
print("Token obtained:", token[:20] + "...")

# Step 2: Test dashboard stats endpoint
req2 = urllib.request.Request("http://localhost:18080/api/dashboard/stats", headers={"Authorization": "Bearer " + token})
try:
    resp2 = urllib.request.urlopen(req2, timeout=10)
    result = json.loads(resp2.read().decode())
    print("Dashboard stats: code=" + str(result.get("code")) + " message=" + result.get("message", ""))
    if result.get("data"):
        d = result["data"]
        print("  totalUsers=" + str(d.get("totalUsers")))
        print("  totalPets=" + str(d.get("totalPets")))
        print("  totalFeeders=" + str(d.get("totalFeeders")))
        print("  totalOrders=" + str(d.get("totalOrders")))
        print("  totalRevenue=" + str(d.get("totalRevenue")))
except urllib.error.HTTPError as e:
    print("Dashboard Error: HTTP " + str(e.code))
    print(e.read().decode()[:500])
except Exception as e:
    print("Dashboard Error: " + str(e))

# Step 3: Test other endpoints
endpoints = [
    "http://localhost:18080/api/user/list?role=OWNER",
    "http://localhost:18080/api/pet/all",
    "http://localhost:18080/api/feeder",
    "http://localhost:18080/api/order/all",
    "http://localhost:18080/api/review/all",
    "http://localhost:18080/api/payment/all",
]
for url in endpoints:
    req = urllib.request.Request(url, headers={"Authorization": "Bearer " + token})
    try:
        resp = urllib.request.urlopen(req, timeout=10)
        result = json.loads(resp.read().decode())
        short = url.split("localhost:18080")[-1]
        print(short + ": code=" + str(result.get("code")))
    except urllib.error.HTTPError as e:
        short = url.split("localhost:18080")[-1]
        print(short + ": HTTP Error " + str(e.code) + " " + e.read().decode()[:200])
    except Exception as e:
        short = url.split("localhost:18080")[-1]
        print(short + ": Error " + str(e))

# Step 4: Test via domain too
req3 = urllib.request.Request("https://mayunfei.asia/api/dashboard/stats", headers={"Authorization": "Bearer " + token})
try:
    resp3 = urllib.request.urlopen(req3, timeout=10, context=ctx)
    result3 = json.loads(resp3.read().decode())
    print("Domain dashboard: code=" + str(result3.get("code")))
except urllib.error.HTTPError as e:
    print("Domain dashboard: HTTP " + str(e.code) + " " + e.read().decode()[:200])
except Exception as e:
    print("Domain dashboard: Error " + str(e))
