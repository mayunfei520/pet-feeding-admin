import urllib.request, json, ssl

ctx = ssl.create_default_context()
ctx.check_hostname = False
ctx.verify_mode = ssl.CERT_NONE

# Login to get token
data = json.dumps({"username": "admin", "password": "123456"}).encode()
req = urllib.request.Request("http://localhost:18080/api/user/login", data=data, headers={"Content-Type": "application/json"})
resp = urllib.request.urlopen(req, timeout=10)
login_result = json.loads(resp.read().decode())
token = login_result["data"]["token"]
print("Token obtained")

# Get full dashboard response
req2 = urllib.request.Request("http://localhost:18080/api/dashboard/stats", headers={"Authorization": "Bearer " + token})
resp2 = urllib.request.urlopen(req2, timeout=10)
raw = resp2.read().decode()
print("Dashboard raw response:")
print(json.dumps(json.loads(raw), indent=2, ensure_ascii=False))

# Also test via domain
req3 = urllib.request.Request("https://mayunfei.asia/api/dashboard/stats", headers={"Authorization": "Bearer " + token})
resp3 = urllib.request.urlopen(req3, timeout=10, context=ctx)
raw3 = resp3.read().decode()
print("\nDomain dashboard raw:")
print(json.dumps(json.loads(raw3), indent=2, ensure_ascii=False))
