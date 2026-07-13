CREATE USER IF NOT EXISTS 'petfeeder'@'%' IDENTIFIED BY 'PetFeeding2024!';
GRANT ALL PRIVILEGES ON pet_feeding.* TO 'petfeeder'@'%';
FLUSH PRIVILEGES;
SELECT user, host FROM mysql.user WHERE user = 'petfeeder';
