import paramiko, time

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('101.42.24.114', username='ubuntu', password='%yzuKJ.L=R7/83')

def run(cmd, use_sudo=False):
    if use_sudo:
        stdin, stdout, stderr = ssh.exec_command(f'sudo {cmd}', get_pty=True)
        stdin.write('%yzuKJ.L=R7/83\n')
        stdin.flush()
        time.sleep(6)
    else:
        stdin, stdout, stderr = ssh.exec_command(cmd)
        time.sleep(4)
    return stdout.read().decode(), stderr.read().decode()

# 1. MySQL charset variables
print("=== MySQL charset variables ===")
sql = "SHOW VARIABLES WHERE Variable_name LIKE 'character%' OR Variable_name LIKE 'collation%';"
out, err = run(f"docker exec pet-feeding-mysql mysql -uroot -p123456 -e \"{sql}\"")
print(out)
if err.strip(): print("ERR:", err[:200])

# 2. Database charset
print("\n=== Database pet_feeding charset ===")
sql2 = "SELECT DEFAULT_CHARACTER_SET_NAME, DEFAULT_COLLATION_NAME FROM information_schema.SCHEMATA WHERE SCHEMA_NAME='pet_feeding';"
out2, err2 = run(f"docker exec pet-feeding-mysql mysql -uroot -p123456 -e \"{sql2}\"")
print(out2)
if err2.strip(): print("ERR:", err2[:200])

# 3. Tables charset
print("\n=== All tables charset ===")
sql3 = ("SELECT TABLE_NAME, CCSA.character_set_name, CCSA.collation_name "
         "FROM information_schema.TABLES T, information_schema.COLLATION_CHARACTER_SET_APPLICABILITY CCSA "
         "WHERE CCSA.collation_name = T.table_collation AND T.table_schema='pet_feeding';")
out3, err3 = run(f"docker exec pet-feeding-mysql mysql -uroot -p123456 -e \"{sql3}\"")
print(out3)
if err3.strip(): print("ERR:", err3[:200])

# 4. Check hex values of Chinese data
print("\n=== Orders address HEX ===")
sql4 = "SELECT id, HEX(address), address FROM orders;"
out4, err4 = run(f"docker exec pet-feeding-mysql mysql -uroot -p123456 pet_feeding -e \"{sql4}\"")
print(out4)
if err4.strip(): print("ERR:", err4[:200])

print("\n=== Pets name HEX ===")
sql5 = "SELECT id, HEX(name), name FROM pets;"
out5, err5 = run(f"docker exec pet-feeding-mysql mysql -uroot -p123456 pet_feeding -e \"{sql5}\"")
print(out5)
if err5.strip(): print("ERR:", err5[:200])

# 5. Check MySQL container locale/env
print("\n=== MySQL container env ===")
out6, err6 = run("docker exec pet-feeding-mysql env")
print(out6)

ssh.close()
print("\nDone.")
