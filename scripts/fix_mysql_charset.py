import paramiko, time

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('101.42.24.114', username='ubuntu', password='%yzuKJ.L=R7/83')

def run_sudo(cmd):
    stdin, stdout, stderr = ssh.exec_command('sudo ' + cmd, get_pty=True)
    stdin.write('%yzuKJ.L=R7/83\n')
    stdin.flush()
    time.sleep(6)
    return stdout.read().decode()

# Write MySQL charset config file
mysql_conf = "[client]\ndefault-character-set=utf8mb4\n\n[mysql]\ndefault-character-set=utf8mb4\n\n[mysqldump]\ndefault-character-set=utf8mb4\n"

sftp = ssh.open_sftp()
with sftp.open('/tmp/mysql-charset.cnf', 'w') as f:
    f.write(mysql_conf)
sftp.close()

# Copy into MySQL container
print("=== Copy charset config ===")
print(run_sudo('docker cp /tmp/mysql-charset.cnf pet-feeding-mysql:/etc/mysql/conf.d/charset.cnf'))

# Restart MySQL to pick up the config
print("=== Restart MySQL ===")
print(run_sudo('docker restart pet-feeding-mysql'))
time.sleep(15)

# Verify charset is now correct
print("=== Verify charset ===")
print(run_sudo('docker exec pet-feeding-mysql mysql -uroot -p123456 -e "SHOW VARIABLES WHERE Variable_name LIKE \'character%\' OR Variable_name LIKE \'collation%\';"'))

# Final API test
print("\n=== Final API test ===")
sftp2 = ssh.open_sftp()
with sftp2.open('/tmp/final_test.sh', 'w') as f:
    f.write("""#!/bin/bash
TOKEN=$(curl -s -X POST http://localhost:18080/api/user/login -H "Content-Type: application/json" -d '{"username":"admin","password":"123456"}' | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['token'])")
echo "Token: ${TOKEN:0:20}..."
echo ""
echo "=== Pets ==="
curl -s http://localhost:18080/api/pet/all -H "Authorization: Bearer $TOKEN" | python3 -c "
import sys,json
data = json.load(sys.stdin)
for p in data.get('data',[])[:3]:
    print('  id=' + str(p['id']) + ' name=' + p['name'] + ' species=' + p['species'] + ' breed=' + str(p.get('breed','')))
"
echo ""
echo "=== Orders ==="
curl -s http://localhost:18080/api/order/all -H "Authorization: Bearer $TOKEN" | python3 -c "
import sys,json
data = json.load(sys.stdin)
for o in data.get('data',[])[:3]:
    print('  id=' + str(o['id']) + ' address=' + o['address'])
"
echo ""
echo "=== Reviews ==="
curl -s http://localhost:18080/api/review/all -H "Authorization: Bearer $TOKEN" | python3 -c "
import sys,json
data = json.load(sys.stdin)
for r in data.get('data',[])[:3]:
    print('  id=' + str(r['id']) + ' content=' + r['content'])
"
""")
sftp2.close()

print(run_sudo('bash /tmp/final_test.sh'))

# Wait for backend to reconnect to MySQL
print("\n=== Backend health check ===")
time.sleep(5)
print(run_sudo('docker logs pet-feeding-backend --tail 5 2>&1'))

ssh.close()
