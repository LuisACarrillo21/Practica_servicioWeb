package distribuidos;

import javax.swing.SwingUtilities;

public class clienteVIP {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new clienteBase("Cliente VIP (HTTP)", tipoC.VIP).setVisible(true));
    }
}
