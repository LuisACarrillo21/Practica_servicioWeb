package distribuidos;

import javax.swing.SwingUtilities;

public class clienteNO {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new clienteBase("Cliente No Cliente (HTTP)", tipoC.NO_CLIENTE).setVisible(true));
    }
}