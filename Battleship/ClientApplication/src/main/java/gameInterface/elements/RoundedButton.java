package gameInterface.elements;

import javax.swing.*;
import java.awt.*;

public class RoundedButton extends JButton {
    private Color pressedBackgroundColor;
    private Color hoverBackgroundColor;
    Color cadetGrey = Color.decode("#91A6AE");
    Color tyrianPurple = Color.decode("#471732");
    Color taupeGray = Color.decode("#7A7885");
    Color mintGreen = Color.decode("#CDF2EB");

    public RoundedButton(String text) {
        super(text);
        setContentAreaFilled(false);
        setBackground(cadetGrey);
        setForeground(tyrianPurple);
        pressedBackgroundColor = taupeGray;
        hoverBackgroundColor = mintGreen;
        init();
    }

    private void init() {
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                setBackground(hoverBackgroundColor);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                setBackground(cadetGrey);
            }
        });

        addChangeListener(e -> {
            if (getModel().isPressed()) {
                setBackground(pressedBackgroundColor);
            } else if (getModel().isRollover()) {
                setBackground(hoverBackgroundColor);
            } else {
                setBackground(cadetGrey);
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (getModel().isArmed()) {
            g.setColor(pressedBackgroundColor);
        } else if (getModel().isRollover()) {
            g.setColor(hoverBackgroundColor);
        } else {
            g.setColor(getBackground());
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
        g2.setColor(getForeground());
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(getText())) / 2;
        int y = (getHeight() + fm.getAscent()) / 2 - 2;
        g2.drawString(getText(), x, y);
        g2.dispose();
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getForeground());
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
        g2.dispose();
    }

    public void setPressedBackgroundColor(Color pressedBackgroundColor) {
        this.pressedBackgroundColor = pressedBackgroundColor;
    }

    public void setHoverBackgroundColor(Color hoverBackgroundColor) {
        this.hoverBackgroundColor = hoverBackgroundColor;
    }
}
