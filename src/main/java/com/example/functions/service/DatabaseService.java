package com.example.functions.service;

import com.example.functions.dto.ProductoDTO;
import com.example.functions.dto.BodegaDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseService {
    
    private Connection getConnection() throws SQLException {
        String url = System.getenv("DB_URL");
        String user = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");
        return DriverManager.getConnection(url, user, password);
    }
    
    public ProductoDTO createProducto(ProductoDTO producto) throws SQLException {
        String sql = "INSERT INTO PRODUCTOS (CODIGO, NOMBRE, DESCRIPCION, PRECIO, STOCK, STOCK_MINIMO, CATEGORIA, ACTIVO) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"ID"})) {
            stmt.setString(1, producto.getCodigo());
            stmt.setString(2, producto.getNombre());
            stmt.setString(3, producto.getDescripcion());
            stmt.setBigDecimal(4, producto.getPrecio());
            stmt.setInt(5, producto.getStock());
            stmt.setInt(6, producto.getStockMinimo());
            stmt.setString(7, producto.getCategoria());
            stmt.setInt(8, producto.getActivo() ? 1 : 0);
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    producto.setId(rs.getLong(1));
                }
            }
        }
        return producto;
    }
    
    public List<ProductoDTO> listProductos() throws SQLException {
        List<ProductoDTO> productos = new ArrayList<>();
        String sql = "SELECT * FROM PRODUCTOS WHERE ACTIVO = 1";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ProductoDTO producto = new ProductoDTO();
                producto.setId(rs.getLong("ID"));
                producto.setCodigo(rs.getString("CODIGO"));
                producto.setNombre(rs.getString("NOMBRE"));
                producto.setDescripcion(rs.getString("DESCRIPCION"));
                producto.setPrecio(rs.getBigDecimal("PRECIO"));
                producto.setStock(rs.getInt("STOCK"));
                producto.setStockMinimo(rs.getInt("STOCK_MINIMO"));
                producto.setCategoria(rs.getString("CATEGORIA"));
                producto.setActivo(rs.getInt("ACTIVO") == 1);
                productos.add(producto);
            }
        }
        return productos;
    }
    
    public ProductoDTO getProducto(Long id) throws SQLException {
        String sql = "SELECT * FROM PRODUCTOS WHERE ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    ProductoDTO producto = new ProductoDTO();
                    producto.setId(rs.getLong("ID"));
                    producto.setCodigo(rs.getString("CODIGO"));
                    producto.setNombre(rs.getString("NOMBRE"));
                    producto.setDescripcion(rs.getString("DESCRIPCION"));
                    producto.setPrecio(rs.getBigDecimal("PRECIO"));
                    producto.setStock(rs.getInt("STOCK"));
                    producto.setStockMinimo(rs.getInt("STOCK_MINIMO"));
                    producto.setCategoria(rs.getString("CATEGORIA"));
                    producto.setActivo(rs.getInt("ACTIVO") == 1);
                    return producto;
                }
            }
        }
        return null;
    }
    
    public ProductoDTO updateProducto(Long id, ProductoDTO producto) throws SQLException {
        String sql = "UPDATE PRODUCTOS SET CODIGO=?, NOMBRE=?, DESCRIPCION=?, PRECIO=?, STOCK=?, STOCK_MINIMO=?, CATEGORIA=?, ACTIVO=? WHERE ID=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, producto.getCodigo());
            stmt.setString(2, producto.getNombre());
            stmt.setString(3, producto.getDescripcion());
            stmt.setBigDecimal(4, producto.getPrecio());
            stmt.setInt(5, producto.getStock());
            stmt.setInt(6, producto.getStockMinimo());
            stmt.setString(7, producto.getCategoria());
            stmt.setInt(8, producto.getActivo() ? 1 : 0);
            stmt.setLong(9, id);
            stmt.executeUpdate();
            producto.setId(id);
        }
        return producto;
    }
    
    public void deleteProducto(Long id) throws SQLException {
        String sql = "DELETE FROM PRODUCTOS WHERE ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }
    
    public BodegaDTO createBodega(BodegaDTO bodega) throws SQLException {
        String sql = "INSERT INTO BODEGAS (CODIGO, NOMBRE, DIRECCION, TELEFONO, CAPACIDAD_MAXIMA, ESPACIO_UTILIZADO, ACTIVO) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"ID"})) {
            stmt.setString(1, bodega.getCodigo());
            stmt.setString(2, bodega.getNombre());
            stmt.setString(3, bodega.getDireccion());
            stmt.setString(4, bodega.getTelefono());
            stmt.setInt(5, bodega.getCapacidadMaxima());
            stmt.setInt(6, bodega.getEspacioUtilizado());
            stmt.setInt(7, bodega.getActivo() ? 1 : 0);
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    bodega.setId(rs.getLong(1));
                }
            }
        }
        return bodega;
    }
    
    public List<BodegaDTO> listBodegas() throws SQLException {
        List<BodegaDTO> bodegas = new ArrayList<>();
        String sql = "SELECT * FROM BODEGAS WHERE ACTIVO = 1";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                BodegaDTO bodega = new BodegaDTO();
                bodega.setId(rs.getLong("ID"));
                bodega.setCodigo(rs.getString("CODIGO"));
                bodega.setNombre(rs.getString("NOMBRE"));
                bodega.setDireccion(rs.getString("DIRECCION"));
                bodega.setTelefono(rs.getString("TELEFONO"));
                bodega.setCapacidadMaxima(rs.getInt("CAPACIDAD_MAXIMA"));
                bodega.setEspacioUtilizado(rs.getInt("ESPACIO_UTILIZADO"));
                bodega.setActivo(rs.getInt("ACTIVO") == 1);
                bodegas.add(bodega);
            }
        }
        return bodegas;
    }
}