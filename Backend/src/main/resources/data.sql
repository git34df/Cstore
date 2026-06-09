INSERT INTO rol (nombre)
SELECT 'ADMIN' WHERE NOT EXISTS (SELECT 1 FROM rol WHERE nombre = 'ADMIN');

INSERT INTO rol (nombre)
SELECT 'USER' WHERE NOT EXISTS (SELECT 1 FROM rol WHERE nombre = 'USER');

INSERT INTO usuario (nombre, numerotelefono, email, password, estado, rol_id)
SELECT 'Administrador', '999999999', 'admin@cstore.com', 'admin123', 'true', id_rol
FROM rol WHERE nombre = 'ADMIN'
AND NOT EXISTS (SELECT 1 FROM usuario WHERE email = 'admin@cstore.com');

INSERT INTO usuario (nombre, numerotelefono, email, password, estado, rol_id)
SELECT 'Usuario', '988888888', 'user@cstore.com', 'user123', 'true', id_rol
FROM rol WHERE nombre = 'USER'
AND NOT EXISTS (SELECT 1 FROM usuario WHERE email = 'user@cstore.com');