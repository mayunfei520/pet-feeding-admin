import urllib.request, json

# Login
data = json.dumps({"username": "admin", "password": "123456"}).encode("utf-8")
req = urllib.request.Request(
    "http://localhost:18080/api/user/login",
    data=data,
    headers={"Content-Type": "application/json; charset=utf-8"}
)
resp = urllib.request.urlopen(req, timeout=10)
login = json.loads(resp.read().decode("utf-8"))
token = login["data"]["token"]
print("Login code=", login["code"])

# Get orders
req2 = urllib.request.Request(
    "http://localhost:18080/api/order/all",
    headers={"Authorization": "Bearer " + token}
)
resp2 = urllib.request.urlopen(req2, timeout=10)
body_bytes = resp2.read()
print("\n=== Orders API response raw headers ===")
print(resp2.headers.get("Content-Type"))
orders_result = json.loads(body_bytes.decode("utf-8"))
print("\n=== Orders data ===")
for o in orders_result.get("data", [])[:3]:
    print("id=", o.get("id"), "address=", repr(o.get("address")), "notes=", repr(o.get("notes")))

# Get pets
req3 = urllib.request.Request(
    "http://localhost:18080/api/pet/all",
    headers={"Authorization": "Bearer " + token}
)
resp3 = urllib.request.urlopen(req3, timeout=10)
pets_result = json.loads(resp3.read().decode("utf-8"))
print("\n=== Pets data ===")
for p in pets_result.get("data", [])[:3]:
    print("id=", p.get("id"), "name=", repr(p.get("name")), "species=", repr(p.get("species")), "breed=", repr(p.get("breed")))

# Get reviews
req4 = urllib.request.Request(
    "http://localhost:18080/api/review/all",
    headers={"Authorization": "Bearer " + token}
)
resp4 = urllib.request.urlopen(req4, timeout=10)
reviews_result = json.loads(resp4.read().decode("utf-8"))
print("\n=== Reviews data ===")
for r in reviews_result.get("data", [])[:3]:
    print("id=", r.get("id"), "content=", repr(r.get("content")))
