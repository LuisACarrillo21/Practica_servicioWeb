package distribuidos;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;

public class clienteBase extends JFrame {

    private static final String ENDPOINT = "http://localhost:8080/solicitudes";

    private final tipoC Cliente;

    private volatile boolean appRunning = true;
    private volatile boolean sending = false;

    private final JButton btnEnviar = new JButton("Enviar");
    private final JButton btnDetener = new JButton("Detener");
    private final JLabel lblTipo = new JLabel();
    private final JLabel lblEstado = new JLabel("Detenido");
    private final JTextArea areaLog = new JTextArea();

    private Thread hiloDuda, hiloQueja, hiloProblema;

    private final HttpClient http = HttpClient.newBuilder().build();

    public clienteBase(String titulo, tipoC Cliente) {
        super(titulo);
        this.Cliente = Cliente;

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(820, 520);
        setLocationRelativeTo(null);

        construirUI();
        conectarEventos();
        iniciarHilos();

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                cerrar();
                dispose();
            }
        });
    }

    private void construirUI() {
        lblTipo.setText("Tipo de cliente: " + Cliente);
        lblTipo.setFont(lblTipo.getFont().deriveFont(Font.BOLD, 14f));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBorder(BorderFactory.createTitledBorder("Control"));
        top.add(lblTipo);
        top.add(Box.createHorizontalStrut(20));
        top.add(btnEnviar);
        top.add(btnDetener);
        top.add(Box.createHorizontalStrut(20));
        top.add(new JLabel("Estado:"));
        top.add(lblEstado);

        areaLog.setEditable(false);
        areaLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(areaLog);
        scroll.setBorder(BorderFactory.createTitledBorder("Salida de solicitudes (HTTP)"));

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(10, 10, 10, 10));
        root.add(top, BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);

        setContentPane(root);
        actualizarUI();
    }

    private void conectarEventos() {
        btnEnviar.addActionListener(e -> {
            sending = true;
            log("=== ENVÍO ACTIVADO (" + Cliente + ") ===");
            actualizarUI();
        });

        btnDetener.addActionListener(e -> {
            sending = false;
            log("=== ENVÍO DETENIDO (" + Cliente + ") ===");
            actualizarUI();
        });
    }

    private void iniciarHilos() {
        hiloDuda = new Thread(() -> loopAsunto(asunto.DUDA, 2600), "WEB-DUDA");
        hiloQueja = new Thread(() -> loopAsunto(asunto.QUEJA, 650), "WEB-QUEJA");
        hiloProblema = new Thread(() -> loopAsunto(asunto.PROBLEMA, 1200), "WEB-PROBLEMA");

        hiloDuda.start();
        hiloQueja.start();
        hiloProblema.start();
    }

    private void loopAsunto(asunto asunt, int intervaloMs) {
        long seq = 1;

        while (appRunning) {
            try {
                if (!sending) { dormir(120); continue; }

                long id = generarId(Cliente, asunt, seq++);
                solicitud s = new solicitud(id, Cliente, asunt, System.currentTimeMillis());

                String json = Json.toJson(s);

                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(ENDPOINT))
                        .header("Content-Type", "application/json; charset=utf-8")
                        .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                        .build();

                HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

                log("Sale -> " + s + " | HTTP " + resp.statusCode());
                dormir(intervaloMs);

            } catch (Exception e) {
                log("ERROR envío (" + Cliente + "," + asunt + "): " + e.getMessage());
                dormir(600);
            }
        }
    }

    private long generarId(tipoC tipo, asunto asunt, long seq) {
        return tipo.code * 1_000_000L + asunt.code * 100_000L + seq;
    }

    private void actualizarUI() {
        SwingUtilities.invokeLater(() -> {
            lblEstado.setText(sending ? "Enviando" : "Detenido");
            btnEnviar.setEnabled(!sending);
            btnDetener.setEnabled(sending);
        });
    }

    private void log(String t) {
        SwingUtilities.invokeLater(() -> {
            areaLog.append(t + "\n");
            areaLog.setCaretPosition(areaLog.getDocument().getLength());
        });
    }

    private void dormir(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    private void cerrar() {
        appRunning = false;
        sending = false;
        if (hiloDuda != null) hiloDuda.interrupt();
        if (hiloQueja != null) hiloQueja.interrupt();
        if (hiloProblema != null) hiloProblema.interrupt();
    }
}
