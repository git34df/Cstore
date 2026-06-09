INSERT INTO rol (nombre)
SELECT 'admin' WHERE NOT EXISTS (SELECT 1 FROM rol WHERE nombre = 'admin');

INSERT INTO rol (nombre)
SELECT 'user' WHERE NOT EXISTS (SELECT 1 FROM rol WHERE nombre = 'user');

INSERT INTO usuario (nombre, numerotelefono, email, password, estado, rol_id)
SELECT 'Administrador', '999999999', 'admin@cstore.com', 'admin123', 'true', id_rol
FROM rol WHERE nombre = 'admin'
AND NOT EXISTS (SELECT 1 FROM usuario WHERE email = 'admin@cstore.com');

INSERT INTO usuario (nombre, numerotelefono, email, password, estado, rol_id)
SELECT 'Usuario', '988888888', 'user@cstore.com', 'user123', 'true', id_rol
FROM rol WHERE nombre = 'user'
AND NOT EXISTS (SELECT 1 FROM usuario WHERE email = 'user@cstore.com');