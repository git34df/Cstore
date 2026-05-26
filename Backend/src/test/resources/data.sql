-- ─────────────────────────────────────────────
--  ROLES
-- ─────────────────────────────────────────────
INSERT INTO rol (nombre) VALUES ('usuario');
INSERT INTO rol (nombre) VALUES ('admin');

-- ─────────────────────────────────────────────
--  USUARIO ADMIN
-- ─────────────────────────────────────────────
INSERT INTO usuario (nombre, numerotelefono, email, password, estado, rol_id)
VALUES ('Admin Test', '999000000', 'admin@cstore.com', 'admin123', 'true',
        (SELECT id_rol FROM rol WHERE nombre = 'admin'));

-- ─────────────────────────────────────────────
--  CATEGORIA de prueba
-- ─────────────────────────────────────────────
INSERT INTO Categoria (nombre) VALUES ('Bebidas');

-- ─────────────────────────────────────────────
--  PRODUCTO de prueba (id categoria = 1 siempre con create-drop)
-- ─────────────────────────────────────────────
INSERT INTO producto (nombre_producto, descripcion, precio, estado, stock, categoria_fk)
VALUES ('Coca Cola', 'Bebida gaseosa 500ml', 5, 'true', 100, 1);