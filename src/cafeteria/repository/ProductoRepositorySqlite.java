package cafeteria.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Menú persistido en SQLite (tablas producto y adicion). */
public class ProductoRepositorySqlite implements ProductoRepository {

    private final Connection cx;

    public ProductoRepositorySqlite(ConexionSqlite conexion) {
        this.cx = conexion.get();
    }

    @Override
    public List<ConfiguracionProducto> listar() {
        List<ConfiguracionProducto> lista = new ArrayList<>();
        try (Statement st = cx.createStatement();
             ResultSet rs = st.executeQuery("SELECT id,tipo,categoria,nombre,descripcion,precio,extra,disponible "
                     + "FROM producto ORDER BY rowid")) {
            while (rs.next()) {
                lista.add(new ConfiguracionProducto(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
                        rs.getString(5), rs.getDouble(6), rs.getString(7), rs.getInt(8) == 1));
            }
        } catch (SQLException e) {
            throw error(e);
        }
        return lista;
    }

    @Override
    public void guardar(ConfiguracionProducto c) {
        String sql = "INSERT INTO producto(id,tipo,categoria,nombre,descripcion,precio,extra,disponible) VALUES(?,?,?,?,?,?,?,?) "
                + "ON CONFLICT(id) DO UPDATE SET tipo=excluded.tipo, categoria=excluded.categoria, nombre=excluded.nombre, "
                + "descripcion=excluded.descripcion, precio=excluded.precio, extra=excluded.extra, disponible=excluded.disponible";
        try (PreparedStatement ps = cx.prepareStatement(sql)) {
            ps.setString(1, c.id);
            ps.setString(2, c.tipo);
            ps.setString(3, c.categoria);
            ps.setString(4, c.nombre);
            ps.setString(5, c.descripcion);
            ps.setDouble(6, c.precio);
            ps.setString(7, c.extra);
            ps.setInt(8, c.disponible ? 1 : 0);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw error(e);
        }
    }

    @Override
    public void eliminar(String id) {
        try (PreparedStatement ps = cx.prepareStatement("DELETE FROM producto WHERE id=?")) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw error(e);
        }
    }

    @Override
    public Map<String, Map<String, Double>> listarAdiciones() {
        Map<String, Map<String, Double>> mapa = new LinkedHashMap<>();
        try (Statement st = cx.createStatement();
             ResultSet rs = st.executeQuery("SELECT tipo,nombre,costo FROM adicion ORDER BY rowid")) {
            while (rs.next()) {
                mapa.computeIfAbsent(rs.getString(1), t -> new LinkedHashMap<>()).put(rs.getString(2), rs.getDouble(3));
            }
        } catch (SQLException e) {
            throw error(e);
        }
        return mapa;
    }

    @Override
    public void guardarAdicion(String tipo, String nombre, double costo) {
        String sql = "INSERT INTO adicion(tipo,nombre,costo) VALUES(?,?,?) ON CONFLICT(tipo,nombre) DO UPDATE SET costo=excluded.costo";
        try (PreparedStatement ps = cx.prepareStatement(sql)) {
            ps.setString(1, tipo);
            ps.setString(2, nombre);
            ps.setDouble(3, costo);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw error(e);
        }
    }

    @Override
    public void eliminarAdicion(String tipo, String nombre) {
        try (PreparedStatement ps = cx.prepareStatement("DELETE FROM adicion WHERE tipo=? AND nombre=?")) {
            ps.setString(1, tipo);
            ps.setString(2, nombre);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw error(e);
        }
    }

    private static IllegalStateException error(SQLException e) {
        return new IllegalStateException("Error de base de datos: " + e.getMessage(), e);
    }
}
