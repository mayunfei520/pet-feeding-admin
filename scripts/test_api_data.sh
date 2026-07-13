#!/bin/bash
# Test API returns correct Chinese data

# Step 1: Login to get token
TOKEN=$(curl -s -X POST http://localhost:18080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}' | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['token'])")

echo "Token obtained: ${TOKEN:0:20}..."

# Step 2: Test each API endpoint
echo ""
echo "=== Orders API ==="
curl -s http://localhost:18080/api/order/all \
  -H "Authorization: Bearer $TOKEN" | python3 -c "
import sys,json
data = json.load(sys.stdin)
for o in data.get('data',[])[:3]:
    print(f\"  id={o['id']} address={o['address']} status={o['status']}\")
"

echo ""
echo "=== Pets API ==="
curl -s http://localhost:18080/api/pet/all \
  -H "Authorization: Bearer $TOKEN" | python3 -c "
import sys,json
data = json.load(sys.stdin)
for p in data.get('data',[])[:3]:
    print(f\"  id={p['id']} name={p['name']} species={p['species']} breed={p.get('breed','N/A')}\")
"

echo ""
echo "=== Reviews API ==="
curl -s http://localhost:18080/api/review/all \
  -H "Authorization: Bearer $TOKEN" | python3 -c "
import sys,json
data = json.load(sys.stdin)
for r in data.get('data',[])[:3]:
    print(f\"  id={r['id']} content={r['content']}\")
"

echo ""
echo "=== Feeders API ==="
curl -s http://localhost:18080/api/feeder \
  -H "Authorization: Bearer $TOKEN" | python3 -c "
import sys,json
data = json.load(sys.stdin)
for f in data.get('data',[])[:3]:
    print(f\"  id={f['id']} name={f.get('real_name','N/A')} area={f.get('service_area','N/A')}\")
"

echo ""
echo "=== Dashboard API ==="
curl -s http://localhost:18080/api/dashboard/stats \
  -H "Authorization: Bearer $TOKEN" | python3 -c "
import sys,json
data = json.load(sys.stdin)
d = data.get('data',{})
print(f\"  totalUsers={d.get('totalUsers')} totalPets={d.get('totalPets')} totalOrders={d.get('totalOrders')}\")
"
