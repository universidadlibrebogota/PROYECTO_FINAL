package cafeteria.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Ventas y cierres de caja persistidos en SQLite. */
public class VentaRepositorySqlite implements VentaRepository {

    private final Connection cx;

    public VentaRepositorySqlite(ConexionSqlite conexion) {
        this.cx = conexion.get();
    }

    @Override
    public void guardar(RegistroVenta v) {
        try {
            cx.setAutoCommit(false);
            try (PreparedStatement ps = cx.prepareStatement(
                    "INSERT INTO venta(id_pago,id_pedido,fecha,subtotal,descuento,total,metodo,promocion) VALUES(?,?,?,?,?,?,?,?)")) {
                ps.setString(1, v.idPago);
                ps.setInt(2, v.idPedido);
                ps.setString(3, ConexionSqlite.texto(v.fechaHora));
                ps.setDouble(4, v.subtotal);
                ps.setDouble(5, v.descuento);
                ps.setDouble(6, v.total);
                ps.setString(7, v.metodoPago);
                ps.setString(8, v.promocion);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = cx.prepareStatement(
                    "INSERT INTO venta_linea(id_pago,producto,cantidad,precio) VALUES(?,?,?,?)")) {
                for (LineaVenta l : v.lineas) {
                    ps.setString(1, v.idPago);
                    ps.setString(2, l.producto);
                    ps.setInt(3, l.cantidad);
                    ps.setDouble(4, l.precioUnitario);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            cx.commit();
        } catch (SQLException e) {
            try {
                cx.rollback();
            } catch (SQLException ignorada) {
                // se informa el error original
            }
            throw new IllegalStateException("Error de base de datos: " + e.getMessage(), e);
        } finally {
            try {
                cx.setAutoCommit(true);
            } catch (SQLException ignorada) {
                // nada
            }
        }
    }

    @Override
    public List<RegistroVenta> listar(LocalDateTime desde, LocalDateTime hasta) {
        List<RegistroVenta> lista = new ArrayList<>();
        String sql = "SELECT id_pago,id_pedido,fecha,subtotal,descuento,total,metodo,promocion FROM venta "
                + "WHERE fecha >= ? AND fecha <= ? ORDER BY fecha";
        try (PreparedStatement ps = cx.prepareStatement(sql)) {
            ps.setString(1, ConexionSqlite.texto(desde));
            ps.setString(2, ConexionSqlite.texto(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String idPago = rs.getString(1);
                    lista.add(new RegistroVenta(idPago, rs.getInt(2), ConexionSqlite.fecha(rs.getString(3)), rs.getDouble(4),
                            rs.getDouble(5), rs.getDouble(6), rs.getString(7), rs.getString(8), lineas(idPago)));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error de base de datos: " + e.getMessage(), e);
        }
        return lista;
    }

    private List<LineaVenta> lineas(String idPago) throws SQLException {
        List<LineaVenta> lineas = new ArrayList<>();
        try (PreparedStatement ps = cx.prepareStatement("SELECT producto,cantidad,precio FROM venta_linea WHERE id_pago=?")) {
            ps.setString(1, idPago);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lineas.add(new LineaVenta(rs.getString(1), rs.getInt(2), rs.getDouble(3)));
                }
            }
        }
        return lineas;
    }

    @Override
    public LocalDateTime ultimoCierre() {
        try (Statement st = cx.createStatement(); ResultSet rs = st.executeQuery("SELECT MAX(fecha) FROM cierre")) {
            if (rs.next() && rs.getString(1) != null) {
                return ConexionSqlite.fecha(rs.getString(1));
            }
            return null;
        } catch (SQLException e) {
            throw new IllegalStateException("Error de base de datos: " + e.getMessage(), e);
        }
    }

    @Override
    public void registrarCierre(LocalDateTime fecha) {
        try (PreparedStatement ps = cx.prepareStatement("INSERT INTO cierre(fecha) VALUES(?)")) {
            ps.setString(1, ConexionSqlite.texto(fecha));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Error de base de datos: " + e.getMessage(), e);
        }
    }
}
