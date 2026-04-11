package distribuidos;

import javax.swing.SwingUtilities;

public class clienteNORMAL {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new clienteBase("Cliente Normal (HTTP)", tipoC.NORMAL).setVisible(true));
    }
}
