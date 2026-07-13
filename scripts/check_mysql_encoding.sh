#!/bin/bash
echo "=== MySQL charset variables ==="
docker exec pet-feeding-mysql mysql -uroot -p123456 -e "SHOW VARIABLES WHERE Variable_name LIKE 'character%' OR Variable_name LIKE 'collation%';"

echo ""
echo "=== Database pet_feeding charset ==="
docker exec pet-feeding-mysql mysql -uroot -p123456 -e "SELECT DEFAULT_CHARACTER_SET_NAME, DEFAULT_COLLATION_NAME FROM information_schema.SCHEMATA WHERE SCHEMA_NAME='pet_feeding';"

echo ""
echo "=== All tables charset ==="
docker exec pet-feeding-mysql mysql -uroot -p123456 -e "SELECT TABLE_NAME, CCSA.character_set_name FROM information_schema.TABLES T, information_schema.COLLATION_CHARACTER_SET_APPLICABILITY CCSA WHERE CCSA.collation_name = T.table_collation AND T.table_schema='pet_feeding';"

echo ""
echo "=== Orders address HEX ==="
docker exec pet-feeding-mysql mysql -uroot -p123456 pet_feeding -e "SELECT id, HEX(address), address FROM orders;"

echo ""
echo "=== Pets name HEX ==="
docker exec pet-feeding-mysql mysql -uroot -p123456 pet_feeding -e "SELECT id, HEX(name), name FROM pets;"

echo ""
echo "=== Reviews content HEX ==="
docker exec pet-feeding-mysql mysql -uroot -p123456 pet_feeding -e "SELECT id, HEX(content), content FROM reviews;"

echo ""
echo "=== Feeders real_name HEX ==="
docker exec pet-feeding-mysql mysql -uroot -p123456 pet_feeding -e "SELECT id, HEX(real_name), real_name, HEX(service_area), service_area FROM feeders;"

echo ""
echo "=== MySQL container env ==="
docker exec pet-feeding-mysql env | head -20

echo ""
echo "=== Docker compose MySQL command ==="
grep -A10 "mysql:" /home/ubuntu/docker-deploy/pet-feeding-admin-docker/docker-compose.yml | head -15
