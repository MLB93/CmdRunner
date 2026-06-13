package gui;

import data.CmdProcess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class OutputFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private final JTextArea textArea;
    private CmdProcess.OutputListener listener;
    private volatile boolean dirty = false;
    private Timer updateTimer;
    private final JScrollBar vScrollBar;
    private volatile boolean userAdjusting = false;

    public static void show(String text, String title, CmdProcess process) {
        OutputFrame frame = new OutputFrame(text, title, process);
        frame.setVisible(true);
    }

    private OutputFrame(String text, String title, CmdProcess process) {
        textArea = new JTextArea(text);
        textArea.setBackground(Color.BLACK);
        textArea.setForeground(Color.GREEN);
        textArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane);
        this.vScrollBar = scrollPane.getVerticalScrollBar();
        this.vScrollBar.addAdjustmentListener(e -> userAdjusting = e.getValueIsAdjusting());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(750, 450);
        setTitle(title);
        setIconImage(TrayIconManager.IMAGE);

        // Create and register listener for live updates if process is provided
        if (process != null) {
            registerListener(process);
        } else {
            this.listener = null;
        }
    }

    private void registerListener(CmdProcess process) {
        this.listener = () -> dirty = true;
        process.addOutputListener(this.listener);
        startUpdateTimer(process);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopUpdateTimer();
                process.removeOutputListener(listener);
            }
        });
    }

    private void startUpdateTimer(CmdProcess process) {
        updateTimer = new Timer(200, e -> {
            if (!dirty) return;
            if (userAdjusting) return; // don't update while user is dragging the scrollbar
            updateTextFromProcess(process);
            dirty = false;
        });
        updateTimer.setRepeats(true);
        updateTimer.start();
    }

    private void stopUpdateTimer() {
        if (updateTimer != null) {
            updateTimer.stop();
            updateTimer = null;
        }
    }

    private void updateTextFromProcess(CmdProcess process) {
        int prevValue = vScrollBar.getValue();
        boolean wasAtBottom = prevValue + vScrollBar.getVisibleAmount() >= vScrollBar.getMaximum();
        textArea.setText(process.getOutput());
        if (wasAtBottom) {
            textArea.setCaretPosition(textArea.getDocument().getLength());
        } else {
            vScrollBar.setValue(Math.min(prevValue, vScrollBar.getMaximum()));
        }
    }

}
