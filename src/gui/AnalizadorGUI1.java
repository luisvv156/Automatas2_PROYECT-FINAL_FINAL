package gui;

import javax.swing.*;
import javax.swing.text.DefaultHighlighter;
import javax.swing.event.*;  // NUEVO: Para DocumentListener y DocumentEvent


import java.awt.*;
import java.awt.event.*;
import lexer.Lexer;
import parser.Parser;
import ast.ProgramNode;
import semantic.SemanticAnalyzer;
import interpreter.Interpreter;
import util.ManejadorErrores;
import util.ErrorSemantico;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class AnalizadorGUI1 extends JFrame {
    private JTextArea codeArea;
    private JTextArea resultArea;
    private JTextArea lineNumbersArea; // NUEVO: Área para números de línea
    private JButton analyzeButton;
    private JButton clearButton;
    private JButton runButton;
    private JLabel statusLabel;
    private ProgramNode currentProgram; // Guardar el programa analizado

    public AnalizadorGUI1() {
        setTitle("Analizador de Código - Compilador");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700); // Un poco más ancho para los números de línea
        setLocationRelativeTo(null);
        
        initComponents();
        layoutComponents();
        setupEvents();
        setupLineNumbers(); // NUEVO: Configurar números de línea
        
        setLocationRelativeTo(null);
    }

    private void initComponents() {
            // Área de código
            codeArea = new JTextArea();
            codeArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
            codeArea.setBackground(new Color(245, 245, 245));
            
            // NUEVO: Área para números de línea
            lineNumbersArea = new JTextArea();
            lineNumbersArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
            lineNumbersArea.setBackground(new Color(240, 240, 240));
            lineNumbersArea.setForeground(Color.GRAY);
            lineNumbersArea.setEditable(false);
            lineNumbersArea.setFocusable(false);
            lineNumbersArea.setBorder(BorderFactory.createEmptyBorder(3, 5, 0, 5));
            
            // Área de resultados
            resultArea = new JTextArea();
            resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            resultArea.setBorder(BorderFactory.createTitledBorder("Resultados del Análisis"));
            resultArea.setEditable(false);
            resultArea.setBackground(new Color(240, 240, 240));
            resultArea.setForeground(new Color(0, 0, 139));
            
            // Botones (igual que antes)
            analyzeButton = new JButton("🔍 Analizar");
            analyzeButton.setToolTipText("Analizar código (Léxico, Sintáctico, Semántico)");
            
            runButton = new JButton("▶️ Ejecutar");
            runButton.setToolTipText("Ejecutar código (si no hay errores)");
            runButton.setEnabled(false);
            
            clearButton = new JButton("🧹 Limpiar");
            clearButton.setToolTipText("Limpiar áreas de texto");
            
            // Etiqueta de estado
            statusLabel = new JLabel("Listo");
            statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        }

    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // NUEVO: Panel para código con números de línea
        JPanel codePanel = new JPanel(new BorderLayout());
        codePanel.setBorder(BorderFactory.createTitledBorder("Código Fuente"));
        
        JScrollPane codeScroll = new JScrollPane(codeArea);
        codeScroll.setRowHeaderView(lineNumbersArea); // NUEVO: Agregar números de línea
        codeScroll.setPreferredSize(new Dimension(0, 350));
        codePanel.add(codeScroll, BorderLayout.CENTER);
        
        mainPanel.add(codePanel, BorderLayout.NORTH);
        
        JScrollPane resultScroll = new JScrollPane(resultArea);
        resultScroll.setPreferredSize(new Dimension(0, 250));
        mainPanel.add(resultScroll, BorderLayout.CENTER);
        
        JPanel southPanel = new JPanel(new BorderLayout(5, 5));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.add(analyzeButton);
        buttonPanel.add(runButton);
        buttonPanel.add(clearButton);
        
        southPanel.add(buttonPanel, BorderLayout.NORTH);
        southPanel.add(statusLabel, BorderLayout.SOUTH);
        
        mainPanel.add(southPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
        
    // NUEVO: Método para configurar números de línea (VERSIÓN CORREGIDA)
     // MÉTODO CORREGIDO CON IMPORTS ADECUADOS
    private void setupLineNumbers() {
        // Actualizar números de línea cuando cambie el texto
        codeArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { 
                updateLineNumbers(); 
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) { 
                updateLineNumbers(); 
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) { 
                updateLineNumbers(); 
            }
        });
        
        // Sincronizar el scroll (versión simplificada)
        codeArea.addCaretListener(e -> {
            try {
                int caretPos = codeArea.getCaretPosition();
                int line = codeArea.getLineOfOffset(caretPos);
                lineNumbersArea.setCaretPosition(lineNumbersArea.getDocument().getLength());
                lineNumbersArea.setCaretPosition(lineNumbersArea.getLineStartOffset(line));
            } catch (Exception ex) {
                // Ignorar errores
            }
        });
        
        updateLineNumbers();
    }

    // NUEVO: Método para actualizar números de línea (VERSIÓN MEJORADA)
    private void updateLineNumbers() {
        SwingUtilities.invokeLater(() -> {
            try {
                String text = codeArea.getText();
                int lineCount = codeArea.getLineCount();
                
                StringBuilder numbers = new StringBuilder();
                for (int i = 1; i <= lineCount; i++) {
                    numbers.append(i).append("\n");
                }
                
                lineNumbersArea.setText(numbers.toString());
                
                // Ajustar el ancho del área de números
                int maxDigits = String.valueOf(lineCount).length();
                int width = Math.max(30, maxDigits * 8 + 10); // Ancho mínimo 30px
                lineNumbersArea.setPreferredSize(new Dimension(width, lineNumbersArea.getHeight()));
                
            } catch (Exception e) {
                // En caso de error, mostrar números básicos
                lineNumbersArea.setText("1\n");
            }
        });
    }

    private void setupEvents() {
        analyzeButton.addActionListener(e -> analyzeCode());
        
        runButton.addActionListener(e -> runCode());
        
        clearButton.addActionListener(e -> {
            codeArea.setText("");
            resultArea.setText("");
            statusLabel.setText("Áreas limpiadas");
            runButton.setEnabled(false);
            currentProgram = null;
            codeArea.getHighlighter().removeAllHighlights(); // NUEVO: Limpiar resaltados
        });
        
        // Ejemplo de código por defecto
        codeArea.setText("var x: int = 10;\nvar y: int = 5;\nvar resultado: int = x + y;\nprint(resultado);\n\nfunction saludar() {\n    print(\"¡Hola desde la función!\");\n}\n\nsaludar();");
    }

    private void analyzeCode() {
        String sourceCode = codeArea.getText().trim();
        if (sourceCode.isEmpty()) {
            showResult("Por favor, ingrese código para analizar.", Color.RED);
            return;
        }
        
        try {
            setStatus("Analizando...", Color.BLUE);
            resultArea.setText(""); // Limpiar resultados anteriores
            codeArea.getHighlighter().removeAllHighlights(); // NUEVO: Limpiar resaltados anteriores
            
            long startTime = System.currentTimeMillis();
            
            // 1. Análisis Léxico
            setStatus("Realizando análisis léxico...", Color.BLUE);
            Lexer lexer = new Lexer(sourceCode);
            resultArea.setText("✓ Análisis léxico completado\n");
            
            // 2. Análisis Sintáctico
            setStatus("Realizando análisis sintáctico...", Color.BLUE);
            Parser parser = new Parser(lexer);
            currentProgram = parser.parse();
            resultArea.append("✓ Análisis sintáctico completado\n");
            
            long parseTime = System.currentTimeMillis();
            
            // 3. Análisis Semántico
            setStatus("Realizando análisis semántico...", Color.BLUE);
            SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
            semanticAnalyzer.analyze(currentProgram);
            
            ManejadorErrores errores = semanticAnalyzer.getManejadorErrores();
            
            long semanticTime = System.currentTimeMillis();
            
            if (errores.hayErrores()) {
                resultArea.append("\n✗ Se encontraron " + errores.getErrores().size() + " errores semánticos:\n");
                for (ErrorSemantico error : errores.getErrores()) {
                    resultArea.append("• Línea " + error.getLinea() + ": " + error.getMensaje() + "\n");
                }
                resultArea.append("\n❌ No se puede ejecutar debido a errores");
                runButton.setEnabled(false);
                setStatus("Análisis completado con errores", Color.RED);
                
                // NUEVO: Resaltar líneas con errores
                highlightErrorLines(errores.getErrores());
            } else {
                resultArea.append("✓ Análisis semántico completado sin errores\n");
                resultArea.append("✓ Tiempo de análisis: " + (semanticTime - startTime) + "ms\n");
                resultArea.append("\n✅ Código listo para ejecutar");
                runButton.setEnabled(true);
                setStatus("Análisis exitoso - Listo para ejecutar", new Color(0, 100, 0));
            }
            
        } catch (Exception ex) {
            showResult("❌ Error durante el análisis: " + ex.getMessage(), Color.RED);
            ex.printStackTrace();
            runButton.setEnabled(false);
            currentProgram = null;
        }
    }

    private void runCode() {
        if (currentProgram == null) {
            showResult("❌ Primero debe analizar el código", Color.RED);
            return;
        }
        
        try {
            setStatus("Ejecutando código...", new Color(0, 100, 0));
            
            // Redirigir la salida estándar para capturar los prints
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            PrintStream oldOut = System.out;
            System.setOut(ps);
            
            resultArea.append("\n\n--- EJECUCIÓN ---\n");
            
            long startTime = System.currentTimeMillis();
            
            // Ejecutar el código con el intérprete
            Interpreter interpreter = new Interpreter();
            interpreter.interpret(currentProgram);
            
            long executionTime = System.currentTimeMillis();
            
            // Restaurar la salida estándar
            System.out.flush();
            System.setOut(oldOut);
            
            // Mostrar la salida capturada
            String output = baos.toString();
            if (!output.trim().isEmpty()) {
                resultArea.append("Salida:\n" + output);
            } else {
                resultArea.append("(No hubo salida)\n");
            }
            
            resultArea.append("\n✓ Ejecución completada en " + (executionTime - startTime) + "ms");
            setStatus("Ejecución completada", new Color(0, 100, 0));
            
        } catch (Exception ex) {
            // Restaurar salida estándar en caso de error
            System.setOut(System.out);
            
            String errorMessage = ex.getMessage();
            if (errorMessage == null) errorMessage = "Error desconocido durante la ejecución";
            
            resultArea.append("\n❌ Error durante la ejecución: " + errorMessage);
            setStatus("Error en ejecución", Color.RED);
            
            // Imprimir stack trace para debugging
            ex.printStackTrace();
        }
    }

    private void setStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }

    private void showResult(String message, Color color) {
        resultArea.setText(message);
        resultArea.setForeground(color);
    }
    // NUEVO: Método para resaltar líneas con errores
    private void highlightErrorLines(java.util.List<ErrorSemantico> errores) {
        // Crear un highlighter para resaltar líneas con errores
        codeArea.getHighlighter().removeAllHighlights();
        
        for (ErrorSemantico error : errores) {
            int lineNumber = error.getLinea() - 1; // Las líneas empiezan en 0
            if (lineNumber >= 0) {
                try {
                    int start = codeArea.getLineStartOffset(lineNumber);
                    int end = codeArea.getLineEndOffset(lineNumber);
                    
                    // Resaltar la línea completa en rojo claro
                    codeArea.getHighlighter().addHighlight(start, end, 
                        new DefaultHighlighter.DefaultHighlightPainter(new Color(255, 200, 200)));
                } catch (Exception ex) {
                    // Si no se puede resaltar, continuar con el siguiente error
                }
            }
        }
    }

    // NUEVO: Método para agregar tooltips a las líneas con errores
    private void addErrorTooltips(java.util.List<ErrorSemantico> errores) {
        // Podríamos agregar tooltips aquí si queremos más interactividad
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            AnalizadorGUI1 gui = new AnalizadorGUI1();
            gui.setVisible(true);
        });
    }
}