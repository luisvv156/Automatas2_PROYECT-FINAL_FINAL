import gui.AnalizadorGUI1;

public class Main {
    public static void main(String[] args) {
        System.out.println("Iniciando Analizador de Código...");
        System.out.println("Cargando interfaz gráfica...");
        
        // Ejecutar la interfaz gráfica en el Event Dispatch Thread
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    // Establecer el look and feel del sistema
                    javax.swing.UIManager.setLookAndFeel(
                        javax.swing.UIManager.getSystemLookAndFeelClassName()
                    );
                    
                    System.out.println("Look and feel configurado");
                    
                    // Crear y mostrar la GUI
                    AnalizadorGUI1 gui = new AnalizadorGUI1();
                    gui.setVisible(true);
                    
                    System.out.println("Interfaz gráfica cargada correctamente");
                    System.out.println("Listo para analizar código");
                    
                } catch (Exception e) {
                    System.err.println("Error al cargar la interfaz gráfica:");
                    e.printStackTrace();
                    
                    // Fallback: mostrar mensaje de error
                    javax.swing.JOptionPane.showMessageDialog(
                        null,
                        "Error al cargar la interfaz gráfica: " + e.getMessage(),
                        "Error",
                        javax.swing.JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });
    }
}
