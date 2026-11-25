package cafeteria.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Historial de pedidos persistido en SQLite. */
public class PedidoRepositorySqlite implements PedidoRepository {

    private final Connection cx;

    public PedidoRepositorySqlite(ConexionSqlite conexion) {
        this.cx = conexion.get();
    }

    @Override
    public int ultimoId() {
        try (Statement st = cx.createStatement(); ResultSet rs = st.executeQuery("SELECT MAX(id) FROM pedido")) {
            int max = rs.next() ? rs.getInt(1) : 0;
            return Math.max(max, 1000);
        } catch (SQLException e) {
            throw new IllegalStateException("Error de base de datos: " + e.getMessage(), e);
        }
    }

    @Override
    public void guardar(RegistroPedido r) {
        String sql = "INSERT INTO pedido(id,estado,total,fecha,detalle,pagado) VALUES(?,?,?,?,?,?) "
                + "ON CONFLICT(id) DO UPDATE SET estado=excluded.estado, total=excluded.total, "
                + "detalle=excluded.detalle, pagado=excluded.pagado";
        try (PreparedStatement ps = cx.prepareStatement(sql)) {
            ps.setInt(1, r.id);
            ps.setString(2, r.estado);
            ps.setDouble(3, r.total);
            ps.setString(4, ConexionSqlite.texto(r.fecha));
            ps.setString(5, r.detalle);
            ps.setInt(6, r.pagado ? 1 : 0);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Error de base de datos: " + e.getMessage(), e);
        }
    }

    @Override
    public List<RegistroPedido> listar() {
        List<RegistroPedido> lista = new ArrayList<>();
        try (Statement st = cx.createStatement();
             ResultSet rs = st.executeQuery("SELECT id,estado,total,fecha,detalle,pagado FROM pedido ORDER BY id DESC")) {
            while (rs.next()) {
                lista.add(new RegistroPedido(rs.getInt(1), rs.getString(2), rs.getDouble(3),
                        ConexionSqlite.fecha(rs.getString(4)), rs.getString(5), rs.getInt(6) == 1));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error de base de datos: " + e.getMessage(), e);
        }
        return lista;
    }
}
