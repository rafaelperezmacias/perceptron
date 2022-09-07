package tests;

import windows.PerceptronWindow;

import javax.swing.*;

public class PerceptronTest {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            System.out.println("Look and feer error!");
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new PerceptronWindow();
            }
        });
    }

}
